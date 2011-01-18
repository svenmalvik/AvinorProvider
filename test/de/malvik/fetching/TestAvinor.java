package de.malvik.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
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
		assertTrue("Empty list", avinorList.size() > 0);
		for (Avinor avinor : avinorList) {
			assertEquals("Wrong airport", "OSL", avinor.data4airport);
			assertNotNull("No schedule time", avinor.lastUpdate);
			Assert.assertNotNull("UniqueId is null", avinor.uniqueId);
			assertNotNull("flightId is null", avinor.flightId);
			assertNotNull("Airline is null", avinor.airline);
			assertNotNull("Domestic is null", avinor.domestic);
			assertNotNull("UniqueId is null", avinor.uniqueId);
			assertNotNull("No schedule time", avinor.scheduleTime);
		}
	}
	
	@After
	public void teardownClass() {
		httpclient.getConnectionManager().shutdown(); 
	}
	
}
