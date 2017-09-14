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
 * File: LoggingUtils.java
 * Created by: mhaimel
 * Created on: 4 Oct 2007
 * CVS: $Id$
 */
package uk.ac.ebi.proteome.util;

import org.apache.log4j.PropertyConfigurator;

/**
 * @author mhaimel
 * @author $Author$
 * @version $Revision$
 */
public class LoggingUtils {
	
	public static String LOG4J_PROPERTIES_FILE = "log4j.properties";
	
	public static void reloadLog4j(){
		PropertyConfigurator.configure(ClassLoader
				.getSystemResource(LOG4J_PROPERTIES_FILE));
	}
	
}
