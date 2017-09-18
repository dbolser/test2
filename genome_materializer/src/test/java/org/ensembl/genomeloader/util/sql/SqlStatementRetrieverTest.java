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

package org.ensembl.genomeloader.util.sql;

import java.util.Map;

import org.ensembl.genomeloader.util.sql.SqlLibrary;
import org.ensembl.genomeloader.util.sql.SqlStatementRetriever;

import junit.framework.TestCase;

/**
 * Testing correct functioning of SQL Statement retriever class
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class SqlStatementRetrieverTest extends TestCase {

  Map<String,String> library = null;

  protected void setUp() throws Exception {
    super.setUp();
    library = SqlStatementRetriever.getLibrary(new MockSqlLibrary());
  }

  public void testGetLibrary() {
    assertStatementExistence("statementOne");
    assertStatementExistence("statementTwo");
    assertStatementEquals("statementTwo", "select 1 from dual");
    assertStatementEquals("statementThree", "select {0} from dual");
  }

  private void assertStatementExistence(String statementKey) {
    assertNotNull("Expected to find a statement under "+statementKey,
        library.get(statementKey));
  }

  private void assertStatementEquals(String statementKey, String expectedStatement) {
    assertEquals("Expected SQL statement not found",
        expectedStatement, library.get(statementKey));
  }

  public static class MockSqlLibrary implements SqlLibrary {
    public String getSqlLocation() {
      return "/uk/ac/ebi/proteome/util/sql/mock.properties";
    }
  }

  public static final String URI = "jdbc:oracle:thin:idmapper_services/textme@rubens.ebi.ac.uk:1521:XE";

  public void testSemiColonPreservation() throws Exception {
  	String sql = library.get("semiColonPreservation");
  	String search = "WHERE section_id = :new.section_id;";
  	boolean containsSemiColon = sql.contains(search);
  	assertTrue("SQL '"+sql+"' did not contain search string "+search+
  			". Semi colon might have been trimmed", containsSemiColon);
  }
}
