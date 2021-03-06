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

import junit.framework.TestCase;

/**
 * Used for testing the database settings
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DatabaseSettingsTest extends TestCase {

	public void testDatabaseSettingRetrieval() {
		String oracleUrl = "jdbc:oracle:thin:something@somewhere/server:1521:INSTANCE";
		DatabaseSettings expected = DatabaseSettings.ORACLE;
		DatabaseSettings actual = DatabaseSettings.getSettingsForUri(oracleUrl);
		assertEquals("Expected settings returned was not Oracle", expected, actual);
	}

	public void testNoDatabaseSettingRetrieval() {
		String oracleUrl = "nojdbc:oracle:thin:something@somewhere/server:1521:INSTANCE";
		DatabaseSettings actual = DatabaseSettings.getSettingsForUri(oracleUrl);
		assertNull("Expected settings returned was not null", actual);
	}
}
