package de.malvik.fetching;

import static org.junit.Assert.assertNotNull;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAvinor {
	
	private static HttpClient httpclient;
	
	@Before
	public void setupClass() {
		httpclient = new DefaultHttpClient();
	}

	@Test
	public void testOsloInfos() {
        String responseBody = Avinor.getData(httpclient, "OSL");
        assertNotNull(responseBody);
        httpclient.getConnectionManager().shutdown(); 
	}
	
	@Test
	public void testOsloDepatures() {
        String responseBody = Avinor.getData(httpclient, "OSL", Avinor.DEPATURE);
        assertNotNull(responseBody);
        httpclient.getConnectionManager().shutdown(); 
	}
	
	@Test
	public void testOsloArrivals() {
        String responseBody = Avinor.getData(httpclient, "OSL", Avinor.ARRIVAL);
        assertNotNull(responseBody);
        httpclient.getConnectionManager().shutdown(); 
	}
	
	@After
	public void teardownClass() {
		httpclient.getConnectionManager().shutdown(); 
	}
	
// * getData(Airport) * getData(Airport, Destination) * getData(Airport, isDepature) * getData(Airport, isDepature, Destination)
	
}
