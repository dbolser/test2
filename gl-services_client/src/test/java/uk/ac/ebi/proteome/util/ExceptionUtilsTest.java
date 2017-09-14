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
 * File: ExceptionUtilsTest.java
 * Created by: dstaines
 * Created on: Apr 15, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.util;

import java.util.List;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.services.ServiceException;
import uk.ac.ebi.proteome.services.sql.SqlServiceException;
import uk.ac.ebi.proteome.services.sql.SqlServiceUncheckedException;

public class ExceptionUtilsTest extends TestCase {

	public void testSimpleException() throws Exception {
		Exception e = new SqlServiceException("Test",
				ServiceException.PROCESS_TRANSIENT);
		Throwable cause = ExceptionUtils.getRootCause(e);
		assertEquals(e, cause);
	}

	public void testSimpleRuntimeException() throws Exception {
		Exception e = new SqlServiceUncheckedException("Test");
		Throwable cause = ExceptionUtils.getRootCause(e);
		assertEquals(e, cause);
	}

	public void testNestedException() throws Exception {
		Exception e1 = new SqlServiceException("Test",
				ServiceException.PROCESS_TRANSIENT);
		Exception e2 = new SqlServiceException("Test2", e1,
				ServiceException.PROCESS_TRANSIENT);
		Throwable cause = ExceptionUtils.getRootCause(e2);
		assertEquals(e1, cause);
	}

	public void testNestedRuntimeException() throws Exception {
		Exception e1 = new SqlServiceUncheckedException("Test");
		Exception e2 = new SqlServiceUncheckedException("Test2", e1);
		Throwable cause = ExceptionUtils.getRootCause(e2);
		assertEquals(e1, cause);
	}

	public void testNestedException2() throws Exception {
		Exception e1 = new SqlServiceException("Test",
				ServiceException.PROCESS_TRANSIENT);
		Exception e2 = new SqlServiceException("Test2", e1,
				ServiceException.PROCESS_TRANSIENT);
		Exception e3 = new SqlServiceException("Test3", e2,
				ServiceException.PROCESS_TRANSIENT);
		Throwable cause = ExceptionUtils.getRootCause(e3);
		assertEquals(e1, cause);
	}

	public void testNestedRuntimeException2() throws Exception {
		Exception e1 = new SqlServiceUncheckedException("Test");
		Exception e2 = new SqlServiceUncheckedException("Test2", e1);
		Exception e3 = new SqlServiceUncheckedException("Test3", e2);
		Throwable cause = ExceptionUtils.getRootCause(e3);
		assertEquals(e1, cause);
	}

	public void testExtractOracleCodes() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not exist");
		List<String> codes = ExceptionUtils.extractAllOracleCodes(e);
		assertEquals(codes.size(),1);
		assertEquals(codes.get(0),"ORA-00942");
	}
	public void testExtractOracleCode() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not exist");
		String codes = ExceptionUtils.extractOracleCode(e);
		assertEquals(codes,"ORA-00942");
	}
	public void testExtractRootOracleCode() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not exist");
		String codes = ExceptionUtils.extractRootOracleCode(e);
		assertEquals(codes,"ORA-00942");
	}

	public void testExtractOracleCodes2() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not exist caused by ORA-00666");
		List<String> codes = ExceptionUtils.extractAllOracleCodes(e);
		assertEquals(codes.size(),2);
		assertEquals(codes.get(0),"ORA-00942");
		assertEquals(codes.get(1),"ORA-00666");
	}

	public void testExtractOracleCode2() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not existcaused by ORA-00666");
		String codes = ExceptionUtils.extractOracleCode(e);
		assertEquals(codes,"ORA-00942");
	}
	public void testExtractRootOracleCode2() throws Exception {
		Exception e = new Exception("ORA-00942: table or view does not exist caused by ORA-00666");
		String codes = ExceptionUtils.extractRootOracleCode(e);
		assertEquals(codes,"ORA-00666");
	}

}
