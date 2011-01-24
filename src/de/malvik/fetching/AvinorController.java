package de.malvik.fetching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AvinorController {

	
	private static final String XPATH_AIRPORT = "//airport/@name";
	private static Logger logger = Logger.getLogger(AvinorController.class.getName());
	private static final String URL_AVINOR = "http://flydata.avinor.no/XmlFeed.asp?";
	private static final String XPATH_LAST_UPDATE = "//airport/flights/@lastUpdate";
	private static final String XPATH_FLIGHT = "//airport/flights/flight";
	public static Boolean ARRIVAL = Boolean.TRUE;
	public static Boolean DEPATURE = Boolean.valueOf(!ARRIVAL.booleanValue());
	private static XPath xPath = XPathFactory.newInstance().newXPath();
	public static String DB_SERVER = "http://localhost:5984/nortrafikk";
	
	public static List<Avinor> getAirportPlan(HttpClient httpclient, String airport) {
		Document doc = getDocument(httpclient, airport);
		return createAvinorList(doc, airport);
	}

	private static List<Avinor> createAvinorList(Document doc, String airport) {
		List<Avinor> list = new ArrayList<Avinor>();
		
		if (isValidAirportPlan(doc, airport)) {
			String lastUpdate = extractString(doc, XPATH_LAST_UPDATE);
			NodeList nodes = extractNodeset(doc, XPATH_FLIGHT);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Avinor avinor = createAvinor(nodes.item(i), airport, lastUpdate);
				list.add(avinor);
			 }
			logger.log(Level.INFO, "Fetch data for airport <" + airport + "> DataSize is " + list.size());
		}
		return list;
	}
	
	private static Avinor createAvinor(Node node, String airport, String lastUpdate) {
		Avinor avinor = new Avinor(airport);
		avinor.setLastUpdate(lastUpdate);
		avinor.map.put("_id", "avinor_" + node.getAttributes().item(0).getNodeValue());
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			avinor.setDataEntity(child.getNodeName(), child.getTextContent());
		}
		return avinor;
	}
	
	private static NodeList extractNodeset(Document doc, String xpath) {
		return (NodeList) extract(doc, XPathConstants.NODESET, xpath);
	}

	private static String extractString(Document doc, String xpath) {
		return (String) extract(doc, XPathConstants.STRING, xpath);
	}

	private static boolean isValidAirportPlan(Document doc, String airport) {
		boolean isValid = doc == null ? false : true;
		String extractedAirport = extractString(doc, XPATH_AIRPORT);
		if (!extractedAirport.equals(airport)) {
			isValid = false;
			logger.log(Level.SEVERE, "Airport is " + extractedAirport + "Should be " + airport);
		}
		return isValid;
	}
	
	public static HttpResponse fetch(HttpClient httpclient, String url) {
		HttpResponse response = null;
		HttpGet httpGet = new HttpGet(url);
		try {
			response = httpclient.execute(httpGet);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage() + "--- URL:" + url, e);
		}

		logger.log(Level.INFO, "Fetched data for url: " + url);
		return response;
	}

	private static HttpResponse fetchAvinor(HttpClient httpclient, String airport) {
		return fetch(httpclient, createUrl(airport, null));
	}

	private static String createUrl(String airport, Boolean isArrival) {
		String arrivalORdepature = "&direction=";
		if (isArrival != null) {
			arrivalORdepature += isArrival.booleanValue() ? "A" : "D";
		}
		return URL_AVINOR + "airport=" + airport + arrivalORdepature;
	}

	private static Object extract(Document doc, QName qname, String xpath) {
		Object result = "";
		try {
			XPathExpression expr = xPath.compile(xpath);
			result = expr.evaluate(doc, qname);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Failed extracting xpath:" + xpath, e);
		}
		return result;
	}

	private static Document getDocument(HttpClient httpclient, String airport) {
		HttpResponse response = fetchAvinor(httpclient, airport);
		validateResponse(response);
		InputStream inputStream = getContentStream(response.getEntity());
		return getDocument(inputStream);
	}

	private static Document getDocument(InputStream inputStream) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could nor parse inputstream", e);
		}
		return doc;
	}

	private static InputStream getContentStream(HttpEntity entity) {
		InputStream inputStream = null;
		try {
			inputStream = entity.getContent();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
		return inputStream;
	}

	private static void validateResponse(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			logger.log(Level.WARNING, "StatusCode:" + statusCode);
		}
	}

	public static void save(HttpClient httpclient, Avinor avinor) {
		try {
			AvinorController.save(httpclient, DB_SERVER, avinor.toJson());
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
	}
	
	private static HttpResponse save(HttpClient httpclient, String url, String json) throws ClientProtocolException, IOException {
		StringEntity reqEntity = createEntity(json);
		HttpUriRequest post = createPost(url, reqEntity);
	    HttpResponse res = httpclient.execute( post, new BasicHttpContext() ); 
	    // @TODO stupid, but comes later
	    post.abort();
	    return res;
	}

	private static HttpUriRequest createPost(String url, StringEntity reqEntity) {
		HttpPost request = new HttpPost( url );
		request.setEntity( reqEntity );
		return request;
	}

	private static StringEntity createEntity(String json) throws UnsupportedEncodingException {
		StringEntity reqEntity = new StringEntity( json );
		reqEntity.setContentType("application/json");
		reqEntity.setContentEncoding( "UTF-8" );
		return reqEntity;
	}

	public static void saveOrUpdate(HttpClient httpclient, Avinor avinor)  {
		try {
			HttpGet get = new HttpGet(DB_SERVER + "/" + avinor.map.get("_id"));
			HttpResponse res = httpclient.execute(get);
			JSONObject jo = getJson(res);
			avinor.map.put("_rev", jo.getString("_rev"));
			// @TODO stupid, but comes later
			get.abort();
			save(httpclient, avinor);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
	}

	private static JSONObject getJson(HttpResponse res) throws IOException,
			JSONException {
		InputStream is = res.getEntity().getContent();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String json = "";
		while ((line = in.readLine()) != null) {
			json += line;
		} 
		return new JSONObject(json);
	}
}