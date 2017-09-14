/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * File: BaseCommandLineParser.java
 * Created by: dstaines
 * Created on: Jan 10, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;

/**
 * Base command line parser to which options can be added. Contains basic
 * options help, verbose, debug, version. Also can be configured to exit
 * automatically.
 * 
 * @author dstaines
 * 
 */
public class BaseCommandLineProcessor {

	public final static String HELP_OPTION_SHORT = "h";

	public final static String HELP_OPTION_LONG = "help";

	public final static String HELP_DES = "Prints this message";

	public final static String VERBOSE_OPTION_SHORT = "v";

	public final static String VERBOSE_OPTION_LONG = "verbose";

	public final static String VERBOSE_DES = "Turns on verbose logging";

	public final static String DEBUG_OPTION_SHORT = "d";

	public final static String DEBUG_OPTION_LONG = "debug";

	public final static String DEBUG_DES = "Turns on debug logging";

	public final static String VERSION_OPTION_SHORT = "V";

	public final static String VERSION_OPTION_LONG = "version";

	public final static String VERSION_DES = "Prints the version number of this program";

	/**
	 * Exit status to use when exiting due to bad usage
	 */
	public final static int USAGE_EXIT_STATUS = 1;

	/**
	 * Exit status to use when exiting due to help request
	 */
	public final static int HELP_EXIT_STATUS = 0;

	/**
	 * Exit status to use when exiting due to version string request
	 */
	public final static int VERSION_EXIT_STATUS = 0;

	protected Options options;

	protected String appName;

	protected String version = "0.1";

	protected boolean autoExit = true;

	/**
	 * 
	 * @param appName
	 * @param version
	 */
	public BaseCommandLineProcessor(String appName, String version) {
		this.appName = appName;
		this.version = version;
		init();
	}

	/**
	 * @param appName
	 */
	public BaseCommandLineProcessor(String appName) {
		this.appName = appName;
		init();
	}

	/**
	 * Add base options
	 * 
	 */
	protected void init() {
		// add help
		getOptions()
				.addOption(
						new Option(HELP_OPTION_SHORT, HELP_OPTION_LONG, false,
								HELP_DES));

		// add version
		getOptions().addOption(
				new Option(VERSION_OPTION_SHORT, VERSION_OPTION_LONG, false,
						VERSION_DES));

		// add verbose
		getOptions().addOption(
				new Option(VERBOSE_OPTION_SHORT, VERBOSE_OPTION_LONG, false,
						VERBOSE_DES));

		// add debug
		getOptions().addOption(
				new Option(DEBUG_OPTION_SHORT, DEBUG_OPTION_LONG, false,
						DEBUG_DES));
	}

	/**
	 * Add an option object
	 * 
	 * @param option
	 */
	public void addOption(Option option) {
		if (option != null) {
			getOptions().addOption(option);
		}
	}

	/**
	 * Get the current object
	 * 
	 * @return
	 */
	public Options getOptions() {
		if (options == null) {
			options = new Options();
		}
		return options;
	}

	/**
	 * Parse the command line. Optionally exits on help, version.
	 * 
	 * @param args
	 * @return command line
	 */
	public CommandLine processCommandLine(String[] args) {
		CommandLine line = null;
		try {
			// parse the command line arguments
			line = new GnuParser().parse(getOptions(), args);
			if (line == null) {
				throw new ParseException("Null command line returned");
			}
			if(line.hasOption(HELP_OPTION_SHORT)) {
				printHelp();
				exitScript(HELP_EXIT_STATUS);
			}
			if(line.hasOption(VERSION_OPTION_SHORT)) {
				printVersion();
				exitScript(VERSION_EXIT_STATUS);
			}
		} catch (ParseException e) {
			System.err.println("Could not parse command line "
					+ StringUtils.join(args, " "));
			System.err.println("Cause was: "+e.getMessage());
			printHelp();
			exitScript(USAGE_EXIT_STATUS);
		}
		return line;
	}

	protected void exitScript(int status) {
		if (autoExit) {
			System.exit(status);
		}
	}

	/**
	 * Print out the help/usage method
	 */
	public void printHelp() {
		new HelpFormatter().printHelp(appName, getOptions());
	}

	/**
	 * Print out the help/usage method
	 */
	public void printVersion() {
		System.out.println("Program: " + appName);
		System.out.println("Version: " + version);
	}

	protected String getAppName() {
		return this.appName;
	}

	protected void setAppName(String appName) {
		this.appName = appName;
	}

	protected String getVersion() {
		return this.version;
	}

	protected void setVersion(String version) {
		this.version = version;
	}

	protected boolean isAutoExit() {
		return this.autoExit;
	}

	protected void setAutoExit(boolean automaticallyExit) {
		this.autoExit = automaticallyExit;
	}

	/**
	 * Run to test this class with your arguments
	 * 
	 * @param args
	 */
	public final static void main(String[] args) {
		BaseCommandLineProcessor proc = new BaseCommandLineProcessor(
				"BaseCommandLineProcessor", "0.07");
		CommandLine line = proc.processCommandLine(args);
		if(line.hasOption(BaseCommandLineProcessor.VERBOSE_OPTION_SHORT)) {
			System.out.println("Verbose logging activated");
		}
		if(line.hasOption(BaseCommandLineProcessor.DEBUG_OPTION_SHORT)) {
			System.out.println("Debugging activated");
		}
 	}

}
