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
 * File: SqlServiceTest.java
 * Created by: dstaines
 * Created on: Oct 5, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.ArrayUtils;

import junit.framework.Assert;
import junit.framework.TestCase;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;

/**
 * @author dstaines
 *
 */
public class SqlServiceTest extends TestCase {

	private String uri;

	private String query;

	private int rowN;

	private int colN;

	public SqlServiceTest(String testName, String uri, String query, int rowN,
			int colN) {
		super(testName);
		this.uri = uri;
		this.query = query;
		this.rowN = rowN;
		this.colN = colN;
	}

	private void testQuery(Connection con) throws SQLException {
		Statement st = con.createStatement();
		Assert.assertNotNull(st);
		Assert.assertTrue(st.execute(query));
		ResultSet rs = st.getResultSet();
		Assert.assertNotNull(rs);
		Assert.assertFalse(rs.getMetaData().getColumnCount() != colN);
		int i = 0;
		while (rs.next()) {
			i++;
		}
		Assert.assertEquals(i, rowN);
	}

	private void testQuery(SqlService srv) throws SqlServiceException {
		Object[][] r = srv.executeSql(uri, query, ArrayUtils.EMPTY_OBJECT_ARRAY);
		Assert.assertNotNull(r);
		Assert.assertFalse(r.length == 0 && r.length == rowN);
		Assert.assertEquals(r[0].length, colN);
	}

	public void testLocalConnection() throws Exception {
		LocalSqlService srv = new LocalSqlService(ServiceContext.getInstance());
		Assert.assertNotNull(srv);
		DatabaseConnection con = srv.openDatabaseConnection(uri);
		Assert.assertNotNull(con);
		testQuery(con);
		srv.releaseConnection(con);
	}

	public void testWSClientConnection() throws Exception {
		LocalSqlService srv = new LocalSqlService(ServiceContext.getInstance());
		Assert.assertNotNull(srv);
		DatabaseConnection con = srv.openDatabaseConnection(uri);
		Assert.assertNotNull(con);
		testQuery(con);
		srv.releaseConnection(con);
	}

	public void testLocalQuery() throws Exception {
		SqlService srv = new LocalSqlService(ServiceContext.getInstance());
		Assert.assertNotNull(srv);
		testQuery(srv);
	}

	public void testWSClientQuery() throws Exception {
//		SqlService srv = new WSClientSqlService(ServiceContext.getInstance());
//		Assert.assertNotNull(srv);
//		testQuery(srv);
	}

}
