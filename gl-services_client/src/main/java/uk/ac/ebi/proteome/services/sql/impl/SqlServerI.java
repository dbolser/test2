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
 * File: SqlServerI.java
 * Created by: dstaines
 * Created on: Nov 9, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql.impl;

import uk.ac.ebi.proteome.services.sql.SqlServiceException;

/**
 * Interface to use with Spring
 *
 * @author dstaines
 *
 */
public interface SqlServerI {

	public abstract Object[][] executeSql(String uri, String sql, Object[] args)
			throws SqlServiceException;

	public abstract Object[][] executeCall(String uri, String sql, Object[] args)
			throws SqlServiceException;

	public abstract Object[][] executeCallWithOutput(String uri, String sql,
			Object[] args, int[] outputTypes) throws SqlServiceException;

	public abstract Object[][] executeSqlNoCache(String uri, String sql)
			throws SqlServiceException;

	public int[] executeTransactionalDml(String uri, String[] statements,
		      Object[][] args, boolean cacheStatements) throws SqlServiceException;

	public int[] executeBatchDml(String uri, String statement, Object[][] args,
			int batchSize) throws SqlServiceException;
}
