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
 * File: MirrorException.java
 * Created by: dstaines
 * Created on: Aug 9, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services;

/**
 * @author dstaines
 * 
 */
public abstract class ServiceException extends Exception {

	/**
	 * an error that occurs which cannot be retried, and means that the entire
	 * application must be halted. This is usually due to a unexpected condition
	 * that the application was not designed to deal with and probably needs
	 * development assistance.
	 */
	public final static int APP_FATAL = 0;

	/**
	 * an error that prevents the current process from completing and is not
	 * transient (e.g. configuration problem) but is restricted to the current
	 * process
	 */
	public final static int PROCESS_FATAL = 1;

	/**
	 * an error that prevents the current process from completing, but due to
	 * reasons that may be transient (e.g. connection failure etc.)
	 */
	public final static int PROCESS_TRANSIENT = 2;

	private int severity = 0;

	/**
	 * 
	 * @param message
	 * @param severity
	 */
	public ServiceException(String message, int severity) {
		super(message);
		this.severity = severity;
	}

	/**
	 * 
	 * @param cause
	 * @param severity
	 */
	public ServiceException(Throwable cause, int severity) {
		super(cause);
		this.severity = severity;
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 * @param severity
	 */
	public ServiceException(String message, Throwable cause, int severity) {
		super(message, cause);
		this.severity = severity;
	}

	/**
	 * Severity of exception
	 * 
	 * @return severity score
	 */
	public int getSeverity() {
		return this.severity;
	}
	

}
