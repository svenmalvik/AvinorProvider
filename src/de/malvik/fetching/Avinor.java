package de.malvik.fetching;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;

public class Avinor {

	public String getData(HttpClient httpclient) {
        String timeFromInHours = "1";
		String timeToInHours = "7";
		String airport = "OSL";
		String lastUpdate = "2009-03-10T15:03:00";
		HttpGet httpget = new HttpGet(
				"http://flydata.avinor.no/XmlFeed.asp?TimeFrom="
						+ timeFromInHours + "&TimeTo=" + timeToInHours
						+ "&airport=" + airport + "&direction=D&lastUpdate="
						+ lastUpdate); 
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
}
