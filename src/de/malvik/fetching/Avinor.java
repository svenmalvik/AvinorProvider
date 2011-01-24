package de.malvik.fetching;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

public class Avinor {

	private static Logger logger = Logger.getLogger(Avinor.class.getName());
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public Map<String, String> map = new HashMap<String, String>();

	public Avinor(String data4airport) {
		map.put("data4airport", data4airport);
	}

	private static Date format(String date) {
		Date formatedDate = new Date();
		try {
			formatedDate = format.parse(date);
		} catch (Exception e) {
			logger.log(Level.WARNING, "MSG:" + e.getMessage() + "--- Date:"
					+ date, e);
		}
		return formatedDate;
	}

	public void setLastUpdate(String lastUpdate) {
		map.put("lastUpdate", format(lastUpdate).toString());
	}

	public void setDataEntity(String name, String value) {
		if (name.equalsIgnoreCase("schedule_time")) {
			map.put("schedule_time", format(value).toString());
			
		} else if (name.equalsIgnoreCase("#text")) {
		
		} else {
			map.put(name, value);
		}
	}

	public String toJson() {
		return new JSONObject(map).toString();
	}
}