package de.malvik;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.impl.client.DefaultHttpClient;

import de.malvik.fetching.Avinor;
import de.malvik.fetching.AvinorController;

public class StartAvinor {
	private static Logger logger = Logger.getLogger(StartAvinor.class.getName());

	public static void main(String[] args) {
		CommandLineParser lvParser = new BasicParser();
        Options lvOptions = setOptions();
        
        try {
        	CommandLine lvCmd = lvParser.parse(lvOptions, args);
        	if (hasHelpOption(lvCmd)) {
        		printHelp(lvOptions);
        		
        	} else {
        		process(lvCmd);
        	}

        } catch (ParseException pvException) {
            printHelp(lvOptions);
            logger.log(Level.SEVERE, "Parse error: " + pvException.getMessage());
        }
	}

	private static void printHelp(Options lvOptions) {
		HelpFormatter lvFormater = new HelpFormatter();
		lvFormater.printHelp(StartAvinor.class.getName(), lvOptions);
	}

	private static void process(CommandLine lvCmd) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, lvCmd.getOptionValue("c"), null);

		for (Avinor avinor : avinorList) {
			AvinorController.saveOrUpdate(httpclient, avinor);
		}  
	}

	private static boolean hasHelpOption(CommandLine lvCmd) {
		return lvCmd.hasOption('h');
	}

	private static Options setOptions() {
		Options lvOptions = new Options();
        Option lvHilfe = new Option("h", "help", false, "Prints all possible parameters.");
        Option lvFormat = new Option("f", "format", true, "Output formats."); lvFormat.setArgName("plain|csv|xml|json");
        Option lvArrival = new Option("a", "arrival", true, "When nothing is set it takes both. (A)rrival, (D)epature"); lvArrival.setArgName("A|D");
        Option lvOutput = new Option("o", "output", true, "When couchdb is set the format is json by default plus the other you set."); lvOutput.setArgName("console|file|couchdb");
        Option lvLocation = new Option("l", "location", true, "CouchDB url when format is couchdb."); lvLocation.setArgName("filePath|couchDBurl");
        Option lvAirport = new Option("c", "airport", true, "<OSL> for <Oslo, Gardermoen>."); lvAirport.setArgName("airportShortName");
        Option lvFlightId = new Option("i", "flightId", true, "Flight number to get data of (SK5479)."); lvFlightId.setArgName("flightId");
        Option lvCountEntries = new Option("n", "countEntries", true, "Limits the output by setting a number of max entries to display."); lvCountEntries.setArgName("number");
        Option lvLastUpdated = new Option("u", "lastUpdated", true, "Avoid traffic when running the programm frequently by getting only updates."); lvLastUpdated.setArgName("10m|2h|3d");
        
        lvOptions.addOption(lvHilfe);
        lvOptions.addOption(lvArrival);
        lvOptions.addOption(lvFormat);
        lvOptions.addOption(lvOutput);
        lvOptions.addOption(lvLocation);
        lvOptions.addOption(lvFlightId);
        lvOptions.addOption(lvCountEntries);
        lvOptions.addOption(lvLastUpdated);
        lvOptions.addOption(lvAirport);
		return lvOptions;
	}
}