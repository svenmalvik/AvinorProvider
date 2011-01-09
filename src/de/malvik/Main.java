package de.malvik;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import de.malvik.fetching.Avinor;

public class Main {

	public static void main(String[] args) {
        HttpClient httpclient = new DefaultHttpClient();
        Avinor avinor = new Avinor();
        String responseBody = avinor.getData(httpclient);
        System.out.println(responseBody);

        httpclient.getConnectionManager().shutdown();     
	}

}
