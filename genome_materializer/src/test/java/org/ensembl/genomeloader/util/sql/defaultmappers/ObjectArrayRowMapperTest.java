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

package org.ensembl.genomeloader.util.sql.defaultmappers;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;

import org.ensembl.genomeloader.services.sql.ROResultSet;

import junit.framework.TestCase;

/**
 * Used to ensure that the object array mapper works as expected
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ObjectArrayRowMapperTest extends TestCase {

	private MockROResultSetGenerator generator = new MockROResultSetGenerator();

	public void testFullRowMapping() {
		generator.addObject("a");
		generator.addObject("b");
		generator.addObject("c");

		ObjectArrayRowMapper mapper = new ObjectArrayRowMapper();
		try {
			Object[] actual = mapper.mapRow(generator.getResultSet(), 1);
			Object[] expected = new Object[]{"a","b","c"};
			assertTrue("Two arrays were not the same", Arrays.deepEquals(expected, actual));
		}
		catch (SQLException e) {
			e.printStackTrace();
			fail("Exception detected");
		}
	}

	public void testPartialRowMapping() {
		generator.addObject("a");
		generator.addObject("b");
		generator.addObject("c");

		ObjectArrayRowMapper mapper = new ObjectArrayRowMapper(new int[]{1,3});
		try {
			Object[] actual = mapper.mapRow(generator.getResultSet(), 1);
			Object[] expected = new Object[]{"a","c"};
			assertTrue("Two arrays were not the same", Arrays.deepEquals(expected, actual));
		}
		catch (SQLException e) {
			e.printStackTrace();
			fail("Exception detected");
		}
	}

	/**
	 * Used to easily setup a one row return result set
	 *
	 * @author ayates
	 * @author $Author$
	 * @version $Revision$
	 */
	public static class MockROResultSetGenerator {

		ROResultSet resultSet = null;

		int currentColumn = 0;

		public MockROResultSetGenerator() {
			resultSet = createMock(ROResultSet.class);
		}

		public void addString(String value) {
			try {
				expect(resultSet.getString(increment())).andReturn(value);
			}
			catch (SQLException e) {
				e.printStackTrace();
				fail("Exception detected");
			}
		}

		public void addObject(Object value) {
			try {
				expect(resultSet.getObject(increment())).andReturn(value);
			}
			catch (SQLException e) {
				e.printStackTrace();
				fail("Exception detected");
			}
		}

		public void addInt(int value) {
			try {
				expect(resultSet.getObject(increment())).andReturn(value);
			}
			catch (SQLException e) {
				e.printStackTrace();
				fail("Exception detected");
			}
		}

		public ROResultSet getResultSet() {
			ResultSetMetaData metaData = createMock(ResultSetMetaData.class);
			try {
				expect(metaData.getColumnCount()).andReturn(currentColumn);
				expect(resultSet.getMetaData()).andReturn(metaData);
			}
			catch(SQLException e) {
				e.printStackTrace();
				fail("Exception detected");
			}

			replay(new Object[]{resultSet, metaData});
			return resultSet;
		}

		public void verifyAndReset() {
			verify(resultSet);
			reset(resultSet);
			resultSet = createMock(ROResultSet.class);
		}

		private int increment() {
			currentColumn++;
			return currentColumn;
		}
	}
}
