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
 * File: SqlServiceTestSuite.java
 * Created by: dstaines
 * Created on: Oct 5, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author dstaines
 * 
 */
public class SqlServiceTestSuite extends TestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for SqlService implementations");
		String uri = "jdbc:oracle:thin:proteomes_prod/pprod@ouzo.ebi.ac.uk:1521:PROT";
		String query = "SELECT MAX(release_id) FROM proteomes.release";
		int rowN = 1;
		int colN = 1;
		suite.addTest(new SqlServiceTest("testLocalConnection", uri, query,
				rowN, colN));
		suite.addTest(new SqlServiceTest("testWSClientConnection", uri, query,
				rowN, colN));
		suite.addTest(new SqlServiceTest("testLocalQuery", uri, query, rowN,
				colN));
		suite.addTest(new SqlServiceTest("testWSClientQuery", uri, query, rowN,
				colN));
		return suite;
	}
}
