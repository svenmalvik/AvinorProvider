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
	private static final String URL_AIRLINES = "http://flydata.avinor.no/airlineNames.asp";
	private static final String XPATH_AIRLINES_NAMES = "//airlineNames/airlineName";
	private static final String URL_STATUSTEXTS = "http://flydata.avinor.no/flightStatuses.asp";
	private static final String XPATH_STATUSTEXTS = "//flightStatuses/flightStatus";
	public static Boolean ARRIVAL = Boolean.TRUE;
	public static Boolean DEPATURE = Boolean.valueOf(!ARRIVAL.booleanValue());
	public static Map<String, Airport> AIRPORTS = new HashMap<String, Airport>();
	public static Map<String, Airline> AIRLINES = new HashMap<String, Airline>();
	public static Map<String, String> STATUSTEXTS = new HashMap<String, String>();
	
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
			flight.setDataEntity(children.item(i));
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
		if (AIRPORTS.size() == 0) {
			Document doc = DataController.getDocument(httpclient, URL_AIRPORTS);
			NodeList airportNames = DataController.extractNodeset(doc, XPATH_AIRPORT_NAMES);
			
			for (int i = 0; i < airportNames.getLength(); i++) {
				Node airportNameNode = airportNames.item(i);
				String airportShortName = airportNameNode.getAttributes().item(0).getNodeValue();
				String airportName = airportNameNode.getAttributes().item(1).getNodeValue();
				
				if (validateName(airportShortName, airportName)) {
					AIRPORTS.put(airportShortName, new Airport(airportShortName, airportName));
					logger.log(Level.INFO, "Fetched airportName <" + airportShortName + "> =  " + airportName);
					
				} else {
					logger.log(Level.WARNING, "Fetching airportNameShort did not work well here");
				}
			 }
		}

		return AIRPORTS;
	}

	private static boolean validateName(String shortName, String longName) {
		boolean hasValidName = true;
		if (shortName == null || shortName.length() == 0 ) {
			hasValidName = false;
		}
		if (longName == null || longName.length() == 0 ) {
			hasValidName = false;
		}
		return hasValidName;
	}
	
	public static Map<String, Airline> getAirlines(HttpClient httpclient) {
		if (AIRLINES.size() == 0) {
			Document doc = DataController.getDocument(httpclient, URL_AIRLINES);
			NodeList airlineNames = DataController.extractNodeset(doc, XPATH_AIRLINES_NAMES);
			
			for (int i = 0; i < airlineNames.getLength(); i++) {
				Node airlineNameNode = airlineNames.item(i);
				String airlineShortName = airlineNameNode.getAttributes().item(0).getNodeValue();
				String airlineName = airlineNameNode.getAttributes().item(1).getNodeValue();
				
				if (validateName(airlineShortName, airlineName)) {
					AIRLINES.put(airlineShortName, new Airline(airlineShortName, airlineName));
					logger.log(Level.INFO, "Fetched airlineName <" + airlineShortName + "> =  " + airlineName);
					
				} else {
					logger.log(Level.WARNING, "Fetching airlineNameShort did not work well here");
				}
			 }
		}

		return AIRLINES;
	}	

	public static Map<String, String> getStatusTexts(HttpClient httpclient) {
		if (STATUSTEXTS.size() == 0) {
			Document doc = DataController.getDocument(httpclient, URL_STATUSTEXTS);
			NodeList statuses = DataController.extractNodeset(doc, XPATH_STATUSTEXTS);
			
			for (int i = 0; i < statuses.getLength(); i++) {
				Node statusNode = statuses.item(i);
				String statusShortName = statusNode.getAttributes().item(0).getNodeValue();
				String statusName = statusNode.getAttributes().item(1).getNodeValue();
				
				if (validateName(statusShortName, statusName)) {
					STATUSTEXTS.put(statusShortName, statusName);
					logger.log(Level.INFO, "Fetched statuses <" + statusShortName + "> =  " + statusName);
					
				} else {
					logger.log(Level.WARNING, "Fetching status did not work well here");
				}
			 }
		}

		return STATUSTEXTS;
	}	

}