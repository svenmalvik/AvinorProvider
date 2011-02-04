package de.malvik.fetching;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
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
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "TRD", null);
		assertTrue("Empty list", avinorList.size() > 0);
		for (Avinor flight : avinorList) {
			assertEquals("Wrong airport for: " + flight.toJson(), "TRD", flight.map.get("data4airport"));
			assertNotNull("No schedule time for: " + flight.toJson(), flight.map.get("lastUpdate"));
			assertNotNull("flightId is null for: " + flight.toJson(), flight.map.get("flight_id"));
			assertNotNull("Airline is null for: " + flight.toJson(), flight.map.get("airline"));
			assertNotNull("Domestic is null for: " + flight.toJson(), flight.map.get("dom_int"));
			assertNotNull("No schedule time for: " + flight.toJson(), flight.map.get("schedule_time"));
		}
	}
	
	@Test
	public void delete() throws ClientProtocolException, IOException, JSONException {
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "OSL", null);
		for (Avinor avinor : avinorList) {
			DataController.delete(httpclient, avinor.map.get("_id"));
		}
	}
	
	@Test
	public void deleteAll() throws ClientProtocolException, IOException, JSONException {
		DataController.deleteAll(httpclient);
	}
	
	@Test
	public void testAvinor2Json()  {
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, "OSL", null);

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