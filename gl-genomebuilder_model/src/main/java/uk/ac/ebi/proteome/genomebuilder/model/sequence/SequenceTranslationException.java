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
 * File: SequenceTranslationException.java
 * Created by: dstaines
 * Created on: Jan 23, 2008
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.model.sequence;

import uk.ac.ebi.proteome.services.ServiceUncheckedException;

/**
 * @author dstaines
 *
 */
public class SequenceTranslationException extends ServiceUncheckedException {

	private static final long serialVersionUID = 6766677196220939820L;

	public SequenceTranslationException() {
	}

	public SequenceTranslationException(String message) {
		super(message);
	}

	public SequenceTranslationException(Throwable cause) {
		super(cause);
	}

	public SequenceTranslationException(String message, Throwable cause) {
		super(message, cause);
	}

}
