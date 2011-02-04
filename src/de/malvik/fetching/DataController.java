package de.malvik.fetching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
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
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DataController {
	
	public static String DB_SERVER = "http://localhost:5984/nortrafikk";
	private static Logger logger = Logger.getLogger(DataController.class.getName());
	private static XPath xPath = XPathFactory.newInstance().newXPath();

	public static String readFromCouchdb(HttpClient httpclient, String id) throws JSONException, IOException  {
		String rev = "";
		HttpResponse res = httpGet(httpclient, DB_SERVER + "/" + id);
		JSONObject jo = getJson(res);
		if (jo.has("_rev")) {
			rev = jo.getString("_rev");
		}
		return rev;
	}
	
	public static HttpResponse httpGet(HttpClient httpclient, String url) {
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

	private static JSONObject getJson(HttpResponse res) throws IOException, JSONException {
		String json = readContent(res);
		return new JSONObject(json);
	}

	public static String readContent(HttpResponse res) throws IOException {
		InputStream is = res.getEntity().getContent();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		String line = "";
		String json = "";
		while ((line = in.readLine()) != null) {
			json += line;
		}
		return json;
	}
	public static void delete(HttpClient httpclient, String id) throws ClientProtocolException, IOException, JSONException {
		String rev = DataController.readFromCouchdb(httpclient, id);
		HttpDelete request = new HttpDelete( DB_SERVER + "/" + id + "?" + "rev=" + rev );
	    HttpResponse res = httpclient.execute( request, new BasicHttpContext() ); 
	    System.out.println(res.getStatusLine());
	    request.abort();
	}
	
	public static void deleteAll(HttpClient httpclient) throws ClientProtocolException, IOException, JSONException {
		HttpGet get = new HttpGet(DB_SERVER + "/_all_docs");
		HttpResponse res = httpclient.execute(get);
		JSONObject json = new JSONObject(DataController.readContent(res));
		JSONArray ja = new JSONArray(json.getString("rows"));
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jo = ja.getJSONObject(i);
			String id = jo.getString("id");
			if (!id.startsWith("_design")) {
				DataController.delete(httpclient, id);
			}
		}
	}
	
	public static Document getDocument(InputStream inputStream) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could nor parse inputstream", e);
		}
		return doc;
	}

	public static InputStream getContentStream(HttpEntity entity) {
		InputStream inputStream = null;
		try {
			inputStream = entity.getContent();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
		return inputStream;
	}

	public static void validateResponse(HttpResponse response) {
		int statusCode = response.getStatusLine().getStatusCode();
		if (statusCode != 200) {
			logger.log(Level.WARNING, "StatusCode:" + statusCode);
		}
	}
	
	public static NodeList extractNodeset(Document doc, String xpath) {
		return (NodeList) extract(doc, XPathConstants.NODESET, xpath);
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

	public static String extractString(Document doc, String xpath) {
		return (String) extract(doc, XPathConstants.STRING, xpath);
	}
	
	public static Document getDocument(HttpClient httpclient, String url, String airport) {
		HttpResponse response = httpGet(httpclient, url);
		DataController.validateResponse(response);
		InputStream inputStream = DataController.getContentStream(response.getEntity());
		return DataController.getDocument(inputStream);
	}

	public static HttpResponse save(HttpClient httpclient, String json) throws ClientProtocolException, IOException {
		StringEntity reqEntity = createEntity(json);
		HttpUriRequest post = createPost(DataController.DB_SERVER, reqEntity);
	    HttpResponse res = httpclient.execute( post, new BasicHttpContext() ); 
	    System.out.println(res.getStatusLine());
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
}