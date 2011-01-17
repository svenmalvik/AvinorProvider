package de.malvik.fetching;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Avinor {

	private static Logger logger = Logger.getLogger(Avinor.class.getName());
	public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public String airport;
	public String uniqueId;
	public String flightId;
	public String airline;
	public String domestic;
	public Date scheduleTime;
	public Date lastUpdate;

	public Avinor(String airport) {
		this.airport = airport;
	}
	
	public void setLastUpdate(String date) {
		try {
			lastUpdate = format.parse(date);
		} catch (Exception e) {
			logger.log(Level.WARNING, "MSG:" + e.getMessage() + "--- Date:" + date, e);
		}
	}
}
