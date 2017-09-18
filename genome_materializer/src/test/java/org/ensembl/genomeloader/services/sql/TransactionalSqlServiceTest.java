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

package org.ensembl.genomeloader.services.sql;

import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.services.config.ServiceConfig;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.SqlServiceException;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.util.sql.TransactionalDmlHolder;

import junit.framework.TestCase;

/**
 * Uses HSQLDB extensivly to test if the transactional quality of the SqlService
 * is true. It requires the LocalSqlService to be used
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class TransactionalSqlServiceTest extends TestCase {

    private Log log = LogFactory.getLog(this.getClass());

    private static final String HSQLDB_DRIVER_NAME = "org.hsqldb.jdbcDriver";
    private static final String HSQLDB_URI = "jdbc:hsqldb:mem:sa@transactional_test";

    private boolean dbSetup = false;

    public TransactionalSqlServiceTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Class.forName(HSQLDB_DRIVER_NAME);
        populateDb();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        destroyDb();
    }

    public void testTransactionalWrite() {
        TransactionalDmlHolder holder = getDefaultDmlHolder();

        try {
            executeDml(holder);
        } catch (SqlServiceException e) {
            log.fatal("Found SqlServiceException when running DML", e);
            fail("Detected SqlServiceException");
        }

        Object[][] expected = new Object[][] { new Object[] { 1, "Mike" }, new Object[] { 2, "Bob" },
                new Object[] { 3, "Bill" }, new Object[] { 4, "Cath" } };
        assertQueryContents("select * from person order by id", expected);
    }

    public void testTransactionalRollback() {
        TransactionalDmlHolder holder = getDefaultDmlHolder();
        holder.addStatement("rubbish won't get processed", null);

        try {
            executeDml(holder);
            fail("Did not throw a SqlServiceException and was the expected flow");
        } catch (SqlServiceException e) {
            // Expected flow of test
        }

        Object[][] expected = new Object[][] { new Object[] { 1, "Andy" }, new Object[] { 2, "Bob" },
                new Object[] { 3, "Bill" }, new Object[] { 4, "Tim" } };
        assertQueryContents("select * from person order by id", expected);
    }

    private TransactionalDmlHolder getDefaultDmlHolder() {
        String updateSql = "update person set name =? where id =?";
        TransactionalDmlHolder holder = new TransactionalDmlHolder();
        holder.addStatement(updateSql, new Object[] { "Mike", 1 });
        holder.addStatement(updateSql, new Object[] { "Cath", 4 });
        return holder;
    }

    protected void populateDb() throws Exception {
        createDb();
        refreshDb();
    }

    protected void destroyDb() throws Exception {
        executeSql("drop table person");
    }

    private void createDb() throws Exception {
        if (!dbSetup) {
            executeSql("create table person (ID INTEGER, NAME VARCHAR(20))");
            dbSetup = true;
        }
    }

    private void refreshDb() throws Exception {
        executeSql("delete from person");
        String insertSql = "insert into person values (?, ?)";
        executeSql(insertSql, new Object[] { 1, "Andy" });
        executeSql(insertSql, new Object[] { 2, "Bob" });
        executeSql(insertSql, new Object[] { 3, "Bill" });
        executeSql(insertSql, new Object[] { 4, "Tim" });
    }

    protected Object[][] executeSql(String sql) throws SqlServiceException {
        return executeSql(sql, null);
    }

    protected Object[][] executeSql(String sql, Object[] args) throws SqlServiceException {
        return getSqlService().executeSql(HSQLDB_URI, sql, args);
    }

    protected int[] executeDml(TransactionalDmlHolder holder) throws SqlServiceException {
        String[] statements = holder.getStatementsArray();
        Object[][] args = holder.getParametersArray();
        return getSqlService().executeTransactionalDml(HSQLDB_URI, statements, args);
    }

    protected void assertQueryContents(String sql, Object[][] expected) {
        Object[][] actual = null;

        try {
            actual = executeSql(sql);
        } catch (SqlServiceException e) {
            log.fatal("Sql problem with " + sql, e);
            fail("Encountered expection when running SQL " + sql);
        }

        assertNotNull("Results array was null but should have been re-assigned during executeSql()", actual);
        assertEquals("Two arrays were not of the same length", expected.length, actual.length);

        for (int i = 0; i < expected.length; i++) {
            log.trace("Expected content: " + Arrays.toString(expected));
            log.trace("Actual content: " + Arrays.toString(actual));
            assertTrue("Level " + i + " (order 0) of the query result arrays were not the same",
                    Arrays.deepEquals(expected[i], actual[i]));
        }
    }

    private SqlService sqlService = null;

    /**
     * Overly complicated bit of reflection fun & games :)
     */
    private SqlService getSqlService() {
        if (sqlService == null) {
            sqlService = new LocalSqlService(getConfig());
        }
        return sqlService;
    }

    private ServiceConfig getConfig() {
        ServiceConfig config = new ServiceConfig();
        config.setMaxDbConnections(1);
        config.setMaxDbConnectionsTotal(1);
        return config;
    }
}
