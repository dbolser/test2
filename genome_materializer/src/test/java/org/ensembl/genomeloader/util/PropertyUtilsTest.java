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

package org.ensembl.genomeloader.util;

import java.text.MessageFormat;

import org.ensembl.genomeloader.services.ServiceUncheckedException;

import junit.framework.TestCase;

/**
 * Set of test cases to be run against the {@link PropertyUtils} class
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class PropertyUtilsTest extends TestCase {

	public void testGetProperties() {
		try {
			String badLocation = "THISLOCATIONWILLNEVEREXIST!!!";
			PropertyUtils.getProperties(badLocation);
			String msg = MessageFormat.format(
					"Location {0} does not exist and "
							+ "should have failed with exception {1}",
					badLocation, ServiceUncheckedException.class.getName());
			fail(msg);
		} catch (ServiceUncheckedException e) {
			// Normal flow
		}
	}

}
