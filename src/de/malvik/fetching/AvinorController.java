package de.malvik.fetching;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.client.HttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AvinorController {
	
	private static final String URL_AIRPORTS = "http://flydata.avinor.no/airportNames.asp";
	private static final String XPATH_AIRPORT = "//airport/@name";
	private static Logger logger = Logger.getLogger(AvinorController.class.getName());
	private static final String URL_FLIGHTS = "http://flydata.avinor.no/XmlFeed.asp?";
	private static final String XPATH_LAST_UPDATE = "//airport/flights/@lastUpdate";
	private static final String XPATH_FLIGHT = "//airport/flights/flight";
	private static final String XPATH_AIRPORT_NAMES = "//airportNames/airportName";
	public static Boolean ARRIVAL = Boolean.TRUE;
	public static Boolean DEPATURE = Boolean.valueOf(!ARRIVAL.booleanValue());
	
	
	public static List<Flight> getAirportPlan(HttpClient httpclient, String airport, Boolean arrival, Date lastUpdated) {
		Document doc = DataController.getDocument(httpclient, createUrl(airport, arrival, lastUpdated));
		return createAvinorList(doc, airport);
	}

	private static List<Flight> createAvinorList(Document doc, String airport) {
		List<Flight> list = new ArrayList<Flight>();
		
		if (isValidAirportPlan(doc, airport)) {
			String lastUpdate = DataController.extractString(doc, XPATH_LAST_UPDATE);
			NodeList nodes = DataController.extractNodeset(doc, XPATH_FLIGHT);
			
			for (int i = 0; i < nodes.getLength(); i++) {
				Flight avinor = createAvinor(nodes.item(i), airport, lastUpdate);
				list.add(avinor);
			 }
			logger.log(Level.INFO, "Fetch data for airport <" + airport + "> DataSize is " + list.size());
		}
		return list;
	}
	
	private static Flight createAvinor(Node node, String airport, String lastUpdate) {
		Flight flight = new Flight(airport);
		flight.setLastUpdate(lastUpdate);
		flight.map.put("_id", "avinor_" + node.getAttributes().item(0).getNodeValue());
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			flight.setDataEntity(child.getNodeName(), child.getTextContent());
		}
		return flight;
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
		return URL_FLIGHTS + "airport=" + airport + formatArrival(isArrival) + formatLastUpdated(lastUpdated);
	}

	private static String formatArrival(Boolean isArrival) {
		String arrivalORdepature = "&direction=";
		if (isArrival != null) {
			arrivalORdepature += isArrival.booleanValue() ? "A" : "D";
		}
		return arrivalORdepature;
	}

	private static String formatLastUpdated(Date lastUpdated) {
		return lastUpdated == null ? "" : "&lastUpdate=" + Flight.DATE_FORMAT.format(lastUpdated);
	}

	public static void saveOrUpdateFlight(HttpClient httpclient, Flight flight)  {
		try {
			String rev = DataController.readFromCouchdb(httpclient, flight.map.get("_id"));
			if (!rev.equalsIgnoreCase("")) {
				flight.map.put("_rev", rev);
			}
			DataController.save(httpclient, flight.toJson());
			
		} catch (Exception e) {
			logger.log(Level.SEVERE, "MSG:" + e.getMessage(), e);
		}
	}

	public static Map<String, Airport> getAirports(HttpClient httpclient) {
		Map<String, Airport> airports = new HashMap<String, Airport>();
		Document doc = DataController.getDocument(httpclient, URL_AIRPORTS);
		NodeList airportNames = DataController.extractNodeset(doc, XPATH_AIRPORT_NAMES);
		
		for (int i = 0; i < airportNames.getLength(); i++) {
			Node airportNameNode = airportNames.item(i);
			String airportShortName = airportNameNode.getAttributes().item(0).getNodeValue();
			String airportName = airportNameNode.getAttributes().item(1).getNodeValue();
			
			if (validateAirportName(airportShortName, airportName)) {
				airports.put(airportShortName, new Airport(airportShortName, airportName));
				logger.log(Level.INFO, "Fetched airportName <" + airportShortName + "> =  " + airportName);
				
			} else {
				logger.log(Level.WARNING, "Fetching airportNameShort did not work well here");
			}
		 }

		return airports;
	}

	private static boolean validateAirportName(String airportShortName, String airportName) {
		boolean hasValidName = true;
		if (airportShortName == null || airportShortName.length() == 0 ) {
			hasValidName = false;
		}
		if (airportName == null || airportName.length() == 0 ) {
			hasValidName = false;
		}
		return hasValidName;
	}
}