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

package uk.ac.ebi.proteome.util.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Allows you to specify a factory for building objects when they are missing
 * from a Map.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface ObjectFactory<T> {

	/**
	 * Builder method. Assumes that since this is default object it cannot
	 * respond to call specific information so it has no parameters
	 */
	T get();

	/**
	 * Common factory which is used for creating a new List whenever called
	 */
	public static class ArrayListFactory<T> implements ObjectFactory<List<T>> {
		public List<T> get() {
			return new ArrayList<T>();
		}
	}

	/**
	 * Common factory which is used for creating a new Set whenever called
	 */
	public static class SetFactory<T> implements ObjectFactory<Set<T>> {
		public Set<T> get() {
			return new HashSet<T>();
		}
	}
}
