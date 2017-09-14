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

package uk.ac.ebi.proteome.services.sql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;
import uk.ac.ebi.proteome.util.sql.BatchDmlHolder;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

/**
 * Used to test batch loading against a HSQLDB instance
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class BatchSqlServiceTest extends TestCase {

	private Log log = LogFactory.getLog(this.getClass());

  private static final String HSQLDB_DRIVER_NAME = "org.hsqldb.jdbcDriver";
  private static final String HSQLDB_URI = "jdbc:hsqldb:mem:sa@batch_test";
  private SqlServiceTemplate template = null;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    template = new SqlServiceTemplateImpl(HSQLDB_URI, getSqlService());
    Class.forName(HSQLDB_DRIVER_NAME);
    template.executeSqlNonCached("create table person (ID INTEGER, NAME VARCHAR(20))");
  }

  @Override
  protected void tearDown() throws Exception {
  	template.executeSqlNonCached("drop table person");
    super.tearDown();
  }

  protected SqlService getSqlService() throws SqlServiceException {
  	return new LocalSqlService(ServiceContext.getInstance());
  }

  public void testBatchLoading() {
  	try {
  		BatchDmlHolder holder = new BatchDmlHolder(10);
  		int expected = 112;
  		for(int i=0; i<expected; i++) {
  			holder.addParams(new Object[]{i, "Name "+i});
  		}
  		String statement = "insert into person values (?,?)";
  		int[] affectedRows = template.executeBatchDml(statement, holder);
  		int totalAffectedRows = 0;
  		for(int affected: affectedRows) totalAffectedRows += affected;
  		assertEquals("Affected rows do not equal each other", expected, totalAffectedRows);

  		int actual = template.queryForDefaultObject("select count(*) from person", Integer.class);
  		assertEquals("Rows inserted into person not as expected", expected, actual);
  	}
  	catch(SqlServiceUncheckedException e) {
  		log.fatal("Exception detected", e);
  		fail("Did not expected exception");
  	}
  }
}
