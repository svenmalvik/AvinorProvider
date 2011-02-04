package de.malvik;

import java.util.List;

import org.apache.http.impl.client.DefaultHttpClient;

import de.malvik.fetching.Avinor;
import de.malvik.fetching.AvinorController;

public class Main {

	public static void main(String[] args) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, argsAirport(args), argsArrival(args));

		for (Avinor avinor : avinorList) {
			save2couchdb(args, httpclient, avinor);
			verboseIfConfigured(args, avinor);
		}  
	}

	private static Boolean argsArrival(String[] args) {
		Boolean arrival = null;
		if (args.length < 4) {
		} else if (args[3].equalsIgnoreCase("A")) {
			arrival = new Boolean(true);
			
		} else if (args[3].equalsIgnoreCase("D")) {
			arrival = new Boolean(false);
		}
		return arrival;
	}

	private static String argsAirport(String[] args) {
		return args[0];
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
