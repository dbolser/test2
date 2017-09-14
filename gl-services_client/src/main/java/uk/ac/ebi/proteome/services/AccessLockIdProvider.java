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

package uk.ac.ebi.proteome.services;

/**
 * Used for generating new lock identifiers according to whatever scheme is
 * required. Defaults are available which can use a job id or a
 * {@link java.util.UUID}
 *
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public interface AccessLockIdProvider {

	/**
	 * Returns a new unique identifier for this process. Subsequent calls
	 * to this method will return new identifiers not the last given.
	 */
	String getNewId();

	/**
	 * Returns the last assigned identifier. Will return null if no identifier
	 * has been requested yet.
	 */
	String getLastAssignedId();
}
