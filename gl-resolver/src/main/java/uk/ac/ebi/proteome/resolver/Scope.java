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
 * File: Scope.java
 * Created by: dstaines
 * Created on: Mar 2, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.resolver;

/**
 * Enumeration of scopes used to classify component data
 * 
 * @author dstaines
 * 
 */
public enum Scope {

	CELLULAR(0, "Cellular organisms"), ORGANELLE(1, "Organelles"), PHAGE(2,
			"Bacteriophages"), VIRUSES(3, "Other viruses");

	private int number;

	private String description;

	Scope(int number, String description) {
		this.number = number;
		this.description = description;
	}

	/**
	 * @return designated number for the scope
	 */
	public int asNumber() {
		return number;
	}

	/**
	 * @return description of component scope
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Return the scope for the given numerical represenation
	 * 
	 * @param n
	 *            numerical representation
	 * @return matching scope or null if not found
	 */
	public static Scope getByNumber(int n) {
		Scope s = null;
		for (Scope scope : Scope.values()) {
			if (scope.number == n) {
				s = scope;
				break;
			}
		}
		return s;
	}

}
