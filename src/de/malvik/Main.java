package de.malvik;

import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;

import de.malvik.fetching.Avinor;
import de.malvik.fetching.AvinorController;

public class Main {

	public static void main(String[] args) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, args[0]);

		for (Avinor avinor : avinorList) {
			save2couchdb(args, httpclient, avinor);
			verboseIfConfigured(args, avinor);
		}  
	}

	private static void save2couchdb(String[] args, DefaultHttpClient httpclient, Avinor avinor) {
		if (args.length > 2 && args[2].equalsIgnoreCase(">cdb")) {
			AvinorController.saveOrUpdate(httpclient, avinor);
		}
	}

	private static void verboseIfConfigured(String[] args, Avinor avinor) {
		if (args.length > 1 && args[1].equalsIgnoreCase("-v")) {
			System.out.println(avinor.toJson());
		}
	}
}
