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

import java.sql.SQLException;

import org.ensembl.genomeloader.services.sql.ROResultSet;
import org.ensembl.genomeloader.util.sql.RowMapper;
import org.ensembl.genomeloader.util.sql.defaultmappers.DefaultObjectRowMapper;

import junit.framework.TestCase;

/**
 * Uses mock classes to fake the output {@link ROResultSet} object and
 * passes this into the default object mapper. This allows us to create a completly
 * offline test case for this code
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class DefaultObjectRowMapperTest extends TestCase {

	private ROResultSet resultSet = null;
	//Used for the iterator
	private int position = 0;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		resultSet = createMock(ROResultSet.class);
	}

	public void testStringMapper() {
		RowMapper<String> mapper = getMapper(String.class);

		runMapperTest(mapper, new RowMapperCallback<String>() {

			public void run(RowMapper<String> mapper) throws SQLException {
				String expected = "expectedString";
				setStringInMock(expected);

				String actual = mapper.mapRow(resultSet, 0);
				assertEquals("Expected and actual String were not the same", expected, actual);
				resetMock();
			}
		});
	}

	public void testNumberBadMapping() {
		RowMapper<Number> mapper = getMapper(Number.class);
		runMapperTest(mapper, new RowMapperCallback<Number>() {
			public void run(RowMapper<Number> mapper) throws SQLException {
				setObjectInMock("barf!");
				assertMapperFailure(mapper);
			}
		});
	}

	/**
	 * Used as a way of removing the problems with exception catching
	 */
	public <T> void runMapperTest(RowMapper<T> mapper, RowMapperCallback<T> callback) {
		try {
			callback.run(mapper);
		}
		catch(SQLException e) {
			e.printStackTrace();
			fail("Did not expect an exception");
		}
	}

	/**
	 * Convenience method for creating a mapper which has a hardcoded column
	 * to 1
	 */
	public <T> RowMapper<T> getMapper(Class<T> expected) {
		return new DefaultObjectRowMapper<T>(expected, 1);
	}

	/**
	 * Sets a String into the mock object and replays it ready for testing
	 */
	public void setStringInMock(String output) throws SQLException {
		expect(resultSet.getString(1)).andReturn(output);
		replay(resultSet);
	}

	/**
	 * Sets a Object into the mock object and replays it ready for testing
	 */
	public void setObjectInMock(Object output) throws SQLException {
		expect(resultSet.getObject(1)).andReturn(output);
		replay(resultSet);
	}

	/**
	 * Verifys the mock state and resets the object
	 *
	 */
	public void resetMock() {
		verify(resultSet);
		reset(resultSet);
	}

	/**
	 * Used when we know that the mapper will fail for some reason. Will fail
	 * if a SQLException was not thrown
	 */
	public <T> void assertMapperFailure(RowMapper<T> mapper) {
		try {
			mapper.mapRow(resultSet, position);
			fail("Did not throw an exception when we expected the mapper to");
		}
		catch(SQLException e) {
			//Expected flow
		}
	}

	public static interface RowMapperCallback<T> {
		void run(RowMapper<T> mapper) throws SQLException;
	}
}
