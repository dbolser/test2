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

import org.ensembl.genomeloader.services.ServiceException;


/**
 * Exception raised when accessing data. May be transient or fatal.
 * @author dstaines
 *
 */
public class SqlServiceException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 * @param severity
	 */
	public SqlServiceException(String message, int severity) {
		super(message, severity);
	}

	/**
	 * @param message
	 * @param cause
	 * @param severity
	 */
	public SqlServiceException(String message, Throwable cause, int severity) {
		super(message, cause, severity);
	}

	/**
	 * @param cause
	 * @param severity
	 */
	public SqlServiceException(Throwable cause, int severity) {
		super(cause, severity);
	}


}
