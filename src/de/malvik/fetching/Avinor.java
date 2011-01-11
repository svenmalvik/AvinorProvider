package de.malvik.fetching;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

public class Avinor {

	private static final String URL_AVINOR = "http://flydata.avinor.no/XmlFeed.asp?";
	public static Boolean ARRIVAL = Boolean.TRUE;
	public static Boolean DEPATURE = Boolean.valueOf(!ARRIVAL.booleanValue());

	public static String getData(HttpClient httpclient, String airport) {
		return getData(httpclient, airport, null);
	}

	public static String getData(HttpClient httpclient, String airport, Boolean isArrival) {
		HttpGet httpget = new HttpGet(createUrl(airport, isArrival));

		ResponseHandler<String> responseHandler = new BasicResponseHandler();
		String responseBody = null;
		try {
			responseBody = httpclient.execute(httpget, responseHandler);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return responseBody;
	}

	private static String createUrl(String airport, Boolean isArrival) {
		String arrivalORdepature = "&direction=";
		if (isArrival != null) {
			arrivalORdepature += isArrival.booleanValue() ? "A" : "D";
		}
		return URL_AVINOR + "airport=" + airport + arrivalORdepature;
	}
}
