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
 * File: LoadModuleSpecifications.java
 * Created by: dstaines
 * Created on: Feb 12, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver.impl;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.resolver.ModuleResolutionException;
import uk.ac.ebi.proteome.resolver.ModuleSpecificationResolver;
import uk.ac.ebi.proteome.resolver.TaskConcernModuleSpecification;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.BaseCommandLineProcessor;

/**
 * Utility class to load module specifications from XML into Hibernate
 * 
 * @author dstaines
 * 
 */
public class LoadModuleSpecifications {

	public static final String APP_NAME = "LoadModuleSpecifications";

	public static final String APP_VERSION = "0.3";

	public static final String LIST_OPTION = "l";

	public static final String LIST_OPTION_LONG = "list";

	public static final String LIST_OPTION_HELP = "list specs from file and db only";

	public static final String REPLACE_OPTION = "r";

	public static final String REPLACE_OPTION_LONG = "replace";

	public static final String REPLACE_OPTION_HELP = "Replace existing specifications in database";

	public static final String SPECFILE_OPTION = "f";

	public static final String SPECFILE_OPTION_LONG = "sourcefile";

	public static final String SPECFILE_OPTION_HELP = "Path to XML spec file";

	public static final String DB_OPTION = "db";

	public static final String DB_OPTION_LONG = "servicesdatabase";

	public static final String DB_OPTION_HELP = "URI of services database";

	public static class ArgProcessor extends BaseCommandLineProcessor {
		public ArgProcessor() {
			super(APP_NAME, APP_VERSION);
		}

		@Override
		protected void init() {
			super.init();
			Option replaceOption = new Option(REPLACE_OPTION,
					REPLACE_OPTION_LONG, false, REPLACE_OPTION_HELP);
			Option sourceFile = new Option(SPECFILE_OPTION,
					SPECFILE_OPTION_LONG, true, SPECFILE_OPTION_HELP);
			Option db = new Option(DB_OPTION, DB_OPTION_LONG, true,
					DB_OPTION_HELP);
			Option list = new Option(LIST_OPTION, LIST_OPTION_LONG, false,
					LIST_OPTION_HELP);
			getOptions().addOption(sourceFile);
			getOptions().addOption(replaceOption);
			getOptions().addOption(db);
			getOptions().addOption(list);

		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// basic properties
		boolean replace = false; // whether to replace existing files or just
		// warn

		String sourceFileName = null; // name of XML file to read from

		String db = null; // URI of services db to write to

		CommandLine line = new ArgProcessor().processCommandLine(args);
		if (line == null) {
			System.err.println("Problem parsing command line");
			System.exit(1);
		}
		if (line.hasOption(REPLACE_OPTION)) {
			replace = true;
		}

		if (line.hasOption(SPECFILE_OPTION)) {
			sourceFileName = line.getOptionValue(SPECFILE_OPTION);
		}

		if (line.hasOption(DB_OPTION)) {
			db = line.getOptionValue(DB_OPTION);
		}

		if (StringUtils.isEmpty(sourceFileName)) {
			System.err.println("Source file not specified!");
			System.exit(1);
		}

		if (StringUtils.isEmpty(db)) {
			System.err.println("Source file not specified!");
			System.exit(1);
		}

		try {
			ServiceContext context = ServiceContext.getInstance();

			if (!StringUtils.isEmpty(sourceFileName)) {
				context.getConfig().setSourceFile(sourceFileName);
			}

			ModuleSpecificationResolver from = null;
			try {
				from = new XMLModuleSpecificationResolver(sourceFileName);
			} catch (IOException e) {
				System.err
						.println("Problem loading XML file " + sourceFileName);
				e.printStackTrace();
				System.exit(254);
			}

			ModuleSpecificationResolver to = new HibernateModuleSpecificationResolver(
					db);

			Collection<TaskConcernModuleSpecification> specs = from
					.getTaskConcernModuleSpecifications();

			System.out.println("Loading specs");

			for (TaskConcernModuleSpecification spec : specs) {

				if (line.hasOption(LIST_OPTION)) {

					System.out.println("From file: " + spec);

				} else {

					if (!replace) {
						if (to.getTaskConcernModuleSpecification(
								spec.getTask(), spec.getConcern()) != null) {
							System.err.println("Spec for " + spec.getTask()
									+ "/" + spec.getConcern()
									+ " already present");
							System.exit(3);
						}
					}

					System.out.println("Writing: " + spec.getTask() + "/"
							+ spec.getConcern());
					if (replace) {
						to.deleteModuleSpecification(spec.getTask(), spec
								.getConcern());
					}
					to.addModuleSpecification(spec);
					if (to.getModuleSpecification(spec.getTask(), spec
							.getConcern()) == null) {
						System.err.println("Failed to write spec "
								+ spec.getTask() + "/" + spec.getConcern());
						System.exit(2);
					}
				}

			}

			if (line.hasOption(LIST_OPTION)) {

				specs = to.getTaskConcernModuleSpecifications();

				for (TaskConcernModuleSpecification spec : specs) {

					System.out.println("From dn: " + spec);
				}
			}

		} catch (ModuleResolutionException e) {
			System.err.println("Could not migrate specs");
			e.printStackTrace();
			System.exit(255);
		}
	}

}
