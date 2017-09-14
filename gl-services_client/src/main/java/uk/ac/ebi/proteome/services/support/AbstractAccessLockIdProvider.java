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

package uk.ac.ebi.proteome.services.support;

import uk.ac.ebi.proteome.services.AccessLockIdProvider;

/**
 * Provides the last id assigned functionality and allows for subclasses
 * to delegate the problem of providing new identifiers
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public abstract class AbstractAccessLockIdProvider implements AccessLockIdProvider {

	private String lastIdAssigned = null;

	public String getNewId() {
		String id = getNewIdDelegate();
		this.lastIdAssigned = id;
		return id;
	}

	public String getLastAssignedId() {
		return lastIdAssigned;
	}

	/**
	 * Returns a new instance of the identifier for use in this class
	 */
	public abstract String getNewIdDelegate();

}
