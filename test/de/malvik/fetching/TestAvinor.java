package de.malvik.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

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
	public void testOsloList() {
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "OSL");
		for (Avinor avinor : avinorList) {
			assertEquals("OSL", avinor.airport);
		}
	}
	
	@After
	public void teardownClass() {
		httpclient.getConnectionManager().shutdown(); 
	}
	
}
