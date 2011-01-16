package de.malvik.fetching;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.w3c.dom.Document;

public class AvinorController {

	private static final String XPATH_AIRPORT = "//airport/@name";
	private static Logger logger = Logger.getLogger(AvinorController.class.getName());
	private static final String URL_AVINOR = "http://flydata.avinor.no/XmlFeed.asp?";
	public static Boolean ARRIVAL = Boolean.TRUE;
	public static Boolean DEPATURE = Boolean.valueOf(!ARRIVAL.booleanValue());
	private static XPath xPath = XPathFactory.newInstance().newXPath();

	public static List<Avinor> getAirportPlan(HttpClient httpclient, String airport) {
		List<Avinor> list = new ArrayList<Avinor>();
		Document doc = getDocument(httpclient, airport);
		boolean isValid = validateAirportPlan(doc, airport);
		if (!isValid) {
			return list;
		}

		// NodeList nodes = (NodeList) result;
		// for (int i = 0; i < nodes.getLength(); i++) {
		// System.out.println(nodes.item(i).getNodeValue());
		// }
		return list;
	}
	
	private static boolean validateAirportPlan(Document doc, String airport) {
		boolean isValid = doc == null ? false : true;
		String extractedAirport = extract(doc, XPATH_AIRPORT);
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

	private static HttpResponse fetchAvinor(HttpClient httpClient, String airport, Boolean isArrival) {
		return fetch(httpClient, createUrl(airport, isArrival));
	}

	private static String createUrl(String airport, Boolean isArrival) {
		String arrivalORdepature = "&direction=";
		if (isArrival != null) {
			arrivalORdepature += isArrival.booleanValue() ? "A" : "D";
		}
		return URL_AVINOR + "airport=" + airport + arrivalORdepature;
	}

	private static String extract(Document doc, String xpath) {
		String result = "";
		try {
			XPathExpression expr = xPath.compile(xpath);
			result = (String) expr.evaluate(doc, XPathConstants.STRING);
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
}
