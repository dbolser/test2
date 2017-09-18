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
 * File: GeneNameType.java
 * Created by: dstaines
 * Created on: Apr 12, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.model;

/**
 * Enumeration for supported gene name types
 *
 * @author dstaines
 *
 */
public enum GeneNameType {

	NAME(1, "Name", "Names"),ORDEREDLOCUSNAMES(2,
			"OrderedLocusName","OrderedLocusNames"), ORFNAMES(3, "ORFName","ORFNames"), OTHER(-1, "OtherName","OtherNames"), SYNONYMS(-2, "Synonym","Synonyms");

	GeneNameType(int n, String name, String altName) {
		this.n = n;
		this.name = name;
		this.altName = altName;
	}

	String name;
	String altName;

	int n;

	public int getAsNumber() {
		return this.n;
	}

	public String getName() {
		return this.name;
	}

	public String getAltName() {
		return this.altName;
	}

	/**
	 * Find the gene name type for the corresponding number from UNIPROT
	 *
	 * @param n
	 *            number of type
	 * @return corresponding gene name type (OTHER if not found)
	 */
	public static GeneNameType getGeneNameTypeByNumber(int n) {
		GeneNameType type = null;
		for (GeneNameType t : GeneNameType.values()) {
			if (n == t.getAsNumber()) {
				type = t;
				break;
			}
		}
		if (type == null)
			type = GeneNameType.OTHER;
		return type;
	}

	/**
	 * Finds the gene name type from the corresponding name as given by
	 * the property of the enum
	 *
	 * @param name specified name
	 * @return type for name (defaults to OTHER if not found)
	 */
	public static GeneNameType getGeneNameTypeByName(String name) {
		GeneNameType type = OTHER;
		for(GeneNameType t: values()) {
			if(t.getName().equals(name) || t.getAltName().equals(name)) {
				type = t;
				break;
			}
		}
		return type;
	}

}
