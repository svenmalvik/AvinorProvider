package de.malvik.fetching;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.w3c.dom.Node;

public class Flight {

	private static Logger logger = Logger.getLogger(Flight.class.getName());
	public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public Map<String, String> map = new HashMap<String, String>();

	public Flight(String data4airport) {
		map.put("data4airport", data4airport);
	}

	private static long format(String date) {
		long formatedDate = 0l;
		try {
			formatedDate = DATE_FORMAT.parse(date).getTime();
		} catch (Exception e) {
			logger.log(Level.WARNING, "MSG:" + e.getMessage() + "--- Date:" + date, e);
		}
		return formatedDate;
	}

	public void setLastUpdate(String lastUpdate) {
		map.put("lastUpdate", String.valueOf(format(lastUpdate)));
	}

	public void setDataEntity(Node entry) {
		String name = entry.getNodeName();
		String value = entry.getTextContent();
		
		if (name.equalsIgnoreCase("schedule_time")) {
			setTime(name, value);
			
		} else if (name.equalsIgnoreCase("#text")) {
		
		} else if (name.equalsIgnoreCase("airport")) {
			setAirportName(name, value);
			
		} else if (name.equalsIgnoreCase("airline")) {
			setAirlineName(name, value);
			
		} else if (name.equalsIgnoreCase("status")) {
			String code = entry.getAttributes().item(0).getNodeValue();
			String time = "";
			if (entry.getAttributes().getLength() > 1) {
				time = entry.getAttributes().item(1).getNodeValue();
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(format(time));
				cal.add(Calendar.HOUR, 1);
				SimpleDateFormat df = new SimpleDateFormat("HH:mm");
				time = ": " + String.valueOf(df.format(cal.getTime()));
			}
			value = AvinorController.STATUSTEXTS.get(code) + time;
			map.put(name, value);
			
		} else {
			map.put(name, value);
		}
	}

	private void setTime(String name, String value) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(format(value));
		cal.add(Calendar.HOUR, 1);
		map.put(name, String.valueOf(cal.getTimeInMillis()));
	}
	
	private void setAirlineName(String name, String value) {
		if (AvinorController.AIRLINES.size() == 0) {
			logger.log(Level.SEVERE, "No airlines found. Please retrieve them first!");
			
		} else {
			map.put(name, AvinorController.AIRLINES.get(value).airlineName);
		}
	}

	private void setAirportName(String name, String value) {
		if (AvinorController.AIRPORTS.size() == 0) {
			logger.log(Level.SEVERE, "No airports found. Please retrieve them first!");
			
		} else {
			map.put(name, AvinorController.AIRPORTS.get(value).airportName);
		}
	}

	public String toJson() {
		return new JSONObject(map).toString();
	}
}