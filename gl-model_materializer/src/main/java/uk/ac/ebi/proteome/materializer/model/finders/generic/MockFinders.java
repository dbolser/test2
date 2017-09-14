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

package uk.ac.ebi.proteome.materializer.model.finders.generic;

import uk.ac.ebi.proteome.persistence.finder.Finder;

import java.util.Collection;
import java.util.Collections;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class MockFinders {

	/**
	 * Returns a finder which will always return null
	 */
	public static <Q,R> Finder<Q,R> create() {
		return new Finder<Q,R>(){
			public R find(Q query) {
				return null;
			}
		};
	}

	/**
	 * Returns a finder which will always return an empty collection
	 */
	public static <Q,R> Finder<Q, Collection<R>> createCollection() {
		return new Finder<Q,Collection<R>>() {
			public Collection<R> find(Q query) {
				return Collections.emptyList();
			}
		};
	}
}
