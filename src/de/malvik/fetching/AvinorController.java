package de.malvik.fetching;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
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
	
	
	public static List<Avinor> getAirportPlan(HttpClient httpclient, String airport, Boolean arrival, Date lastUpdated) {
		Document doc = DataController.getDocument(httpclient, createUrl(airport, arrival, lastUpdated), airport);
		return createAvinorList(doc, airport);
	}

	private static List<Avinor> createAvinorList(Document doc, String airport) {
		List<Avinor> list = new ArrayList<Avinor>();
		
		if (isValidAirportPlan(doc, airport)) {
			String lastUpdate = DataController.extractString(doc, XPATH_LAST_UPDATE);
			NodeList nodes = DataController.extractNodeset(doc, XPATH_FLIGHT);
			
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
	
	private static boolean isValidAirportPlan(Document doc, String airport) {
		boolean isValid = doc == null ? false : true;
		String extractedAirport = DataController.extractString(doc, XPATH_AIRPORT);
		if (!extractedAirport.equals(airport)) {
			isValid = false;
			logger.log(Level.SEVERE, "Airport is " + extractedAirport + "Should be " + airport);
		}
		return isValid;
	}

	private static String createUrl(String airport, Boolean isArrival, Date lastUpdated) {
		return URL_AVINOR + "airport=" + airport + formatArrival(isArrival) + formatLastUpdated(lastUpdated);
	}

	private static String formatArrival(Boolean isArrival) {
		String arrivalORdepature = "&direction=";
		if (isArrival != null) {
			arrivalORdepature += isArrival.booleanValue() ? "A" : "D";
		}
		return arrivalORdepature;
	}

	private static String formatLastUpdated(Date lastUpdated) {
		return lastUpdated == null ? "" : "&lastUpdate=" + Avinor.DATE_FORMAT.format(lastUpdated);
	}

	public static void saveOrUpdate(HttpClient httpclient, Avinor avinor)  {
		try {
			String rev = DataController.readFromCouchdb(httpclient, avinor.map.get("_id"));
			if (!rev.equalsIgnoreCase("")) {
				avinor.map.put("_rev", rev);
			}
			DataController.save(httpclient, avinor.toJson());
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
	}
}