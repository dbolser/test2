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
 * DuplicateIdException
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package uk.ac.ebi.proteome.materializer.ena;

/**
 * @author dstaines
 *
 */
public class DuplicateIdException extends EnaParsingException {

	/**
	 * 
	 */
	public DuplicateIdException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateIdException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public DuplicateIdException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DuplicateIdException(Throwable cause) {
		super(cause);
	}

}
