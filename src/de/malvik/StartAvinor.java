package de.malvik;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.http.impl.client.DefaultHttpClient;

import de.malvik.fetching.Avinor;
import de.malvik.fetching.AvinorController;

public class StartAvinor {
	private static final String DEFAULT_AIRPORT = "OSL";
	private static final String OUTPUT_CONSOLE = "console";
	private static final String OUTPUT_COUCHDB = "couchdb";
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

        } catch (Exception e) {
            printHelp(lvOptions);
            logger.log(Level.SEVERE, "Error: " + e.getMessage());
        }
	}

	private static void printHelp(Options lvOptions) {
		HelpFormatter lvFormater = new HelpFormatter();
		lvFormater.printHelp(StartAvinor.class.getName(), lvOptions);
	}

	private static void process(CommandLine lvCmd) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		List<Avinor> avinorList = AvinorController.getAirportPlan(httpclient, lvCmd.getOptionValue("c", DEFAULT_AIRPORT), getArrival(lvCmd), getLastUpdated(lvCmd));

		for (Avinor avinor : avinorList) {
			processOutput(lvCmd, httpclient, avinor);
		}  
	}

	private static Date getLastUpdated(CommandLine lvCmd) {
		Calendar londonCalendar = getLondonCalendar();
		
		if (hasLastUpdatedOption(lvCmd)) {
			String lastUpdatedInput = lvCmd.getOptionValue('u', "3d");
			int value = Integer.parseInt(lastUpdatedInput.substring(0, lastUpdatedInput.length()-1));
			
			if (lastUpdatedInput.endsWith("m")) {
				londonCalendar.add(Calendar.MINUTE, -1 * value);
				
			} else if (lastUpdatedInput.endsWith("h")) {
				londonCalendar.add(Calendar.HOUR, -1 * value);

			} else if (lastUpdatedInput.endsWith("d")) {
				londonCalendar.add(Calendar.DAY_OF_MONTH, -1 * value);
			
			} else {
				throw new RuntimeException("Update <" + lastUpdatedInput + "> does not work");
			}
		}
		return londonCalendar.getTime();
	}

	private static Calendar getLondonCalendar() {
		Calendar now = Calendar.getInstance();
		now.setTimeZone(TimeZone.getTimeZone("GMT"));
		now.add(Calendar.HOUR, -1);
		return now;
	}

	private static boolean hasLastUpdatedOption(CommandLine lvCmd) {
		return lvCmd.hasOption('u');
	}

	private static void processOutput(CommandLine lvCmd, DefaultHttpClient httpclient, Avinor avinor) {
		boolean print2console = true;
		boolean save2couchdb = true;
		
		if (hasOutputOption(lvCmd)) {
			String output = lvCmd.getOptionValue("o", "");
			
			if (output.equalsIgnoreCase(OUTPUT_COUCHDB)) {
				save2couchdb = true;
				print2console = false;
				
			} else if (output.equalsIgnoreCase(OUTPUT_CONSOLE)) {
				print2console = true;
				save2couchdb = false;
			
			} else {
				throw new RuntimeException("Output <" + output + "> does not exist");
			}			
		}
		
		if (save2couchdb) {
			AvinorController.saveOrUpdate(httpclient, avinor);
		}
		
		if (print2console) {
			System.out.println(avinor.toJson());
		}		
	}

	private static boolean hasOutputOption(CommandLine lvCmd) {
		return lvCmd.hasOption('o');
	}

	private static Boolean getArrival(CommandLine lvCmd) {
		Boolean isArrival = null;
		if (lvCmd.hasOption('a')) {
			String arrival = lvCmd.getOptionValue('a');
			if (arrival.equalsIgnoreCase("A")) {
				isArrival = new Boolean(true);
			
			} else if (arrival.equalsIgnoreCase("D")) {
				isArrival = new Boolean(false);
			}
		}
		return isArrival;
	}

	private static boolean hasHelpOption(CommandLine lvCmd) {
		return lvCmd.hasOption('h');
	}

	private static Options setOptions() {
		Options lvOptions = new Options();
        Option lvHilfe = new Option("h", "help", false, "Prints all possible parameters.");
        Option lvArrival = new Option("a", "arrival", true, "When nothing is set it takes both. (A)rrival, (D)epature"); lvArrival.setArgName("A|D");
        Option lvOutput = new Option("o", "output", true, "Prints the json result to the console or saves in couchdb. If not set it takes both."); lvOutput.setArgName(OUTPUT_CONSOLE + "|" + OUTPUT_COUCHDB);
        Option lvAirport = new Option("c", "airport", true, "<OSL> for <Oslo, Gardermoen>."); lvAirport.setArgName("airportShortName");
        Option lvLastUpdated = new Option("u", "lastUpdated", true, "Avoid traffic when running the programm frequently by getting only updates."); lvLastUpdated.setArgName("10m|2h|3d");
        
        lvOptions.addOption(lvHilfe);
        lvOptions.addOption(lvArrival);
        lvOptions.addOption(lvOutput);
        lvOptions.addOption(lvAirport);
        lvOptions.addOption(lvLastUpdated);

		return lvOptions;
	}
}