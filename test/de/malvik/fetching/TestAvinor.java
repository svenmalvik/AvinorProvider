package de.malvik.fetching;

import static org.junit.Assert.*;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.Test;

public class TestAvinor {

	@Test
	public void testGetData() {
        HttpClient httpclient = new DefaultHttpClient();
        Avinor avinor = new Avinor();
        String responseBody = avinor.getData(httpclient);
        httpclient.getConnectionManager().shutdown(); 
        
        assertNotNull(responseBody);
	}

	
}
