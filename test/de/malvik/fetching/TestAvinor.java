package de.malvik.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestAvinor {
	
	private static HttpClient httpclient;
	
	@Before
	public void setupClass() {
		httpclient = new DefaultHttpClient();
	}
	
	@Ignore
	@Test
	public void testOsloList() {
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "OSL");
		assertTrue("Empty list", avinorList.size() > 0);
		for (Avinor flight : avinorList) {
			assertEquals("Wrong airport", "OSL", flight.map.get("data4airport"));
			assertNotNull("No schedule time", flight.map.get("lastUpdate"));
			assertNotNull("UniqueId is null", flight.map.get("uniqueId"));
			assertNotNull("flightId is null", flight.map.get("flight_id"));
			assertNotNull("Airline is null", flight.map.get("airline"));
			assertNotNull("Domestic is null", flight.map.get("dom_int"));
			assertNotNull("No schedule time", flight.map.get("schedule_time"));
		}
	}
	
	@Test
	public void testAvinor2Json() throws URISyntaxException, IOException, InterruptedException {
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "OSL");

		for (Avinor avinor : avinorList) {
			String json = avinor.toJson();
			assertNotNull(json);
			assertTrue(json.length() > 0);
			
			AvinorController.saveOrUpdate(httpclient, avinor);
		}
	}
	
	@After
	public void teardownClass() {
		httpclient.getConnectionManager().shutdown(); 
	}
	
}
