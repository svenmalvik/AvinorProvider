package de.malvik.fetching;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Avinor {

	private static Logger logger = Logger.getLogger(Avinor.class.getName());
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public String data4airport;
	public String uniqueId;
	public String flightId;
	public String airline;
	public String domestic;
	public String belt;
	public String airport;
	public Date scheduleTime;
	public Date lastUpdate;

	public Avinor(String data4airport) {
		this.data4airport = data4airport;
	}

	public void setLastUpdate(String date) {
		try {
			lastUpdate = format.parse(date);
		} catch (Exception e) {
			logger.log(Level.WARNING, "MSG:" + e.getMessage() + "--- Date:"
					+ date, e);
		}
	}

	public void setDataEntity(String name, String value) {
		if (name.equalsIgnoreCase("airline")) {
			airline = value;
			
		} else if (name.equalsIgnoreCase("flight_id")) {
			flightId = value;
			
		} else if (name.equalsIgnoreCase("schedule_time")) {
			flightId = value;
			
		} else if (name.equalsIgnoreCase("arr_dep")) {
			flightId = value;
			
		} else if (name.equalsIgnoreCase("belt")) {
			flightId = value;
			
		} else if (name.equalsIgnoreCase("airport")) {
			airport = value;
			
		} else if (name.equalsIgnoreCase("dom_int")) {
			domestic = value;
			
		} else if (name.equalsIgnoreCase("status")) {
			domestic = value;
			
		} else if (name.equalsIgnoreCase("gate")) {
			domestic = value;			
			
		} else if (name.equalsIgnoreCase("check_in")) {
			domestic = value;	
			
		} else if (name.equalsIgnoreCase("delayed")) {
			domestic = value;	
			
		} else if (name.equalsIgnoreCase("via_airport")) {
			domestic = value;	
		
		} else if (!name.equalsIgnoreCase("#text")) {
			logger.log(Level.INFO, "Value cannot be set for name <" + name + "> Value is " + value);
		}
	}
}
