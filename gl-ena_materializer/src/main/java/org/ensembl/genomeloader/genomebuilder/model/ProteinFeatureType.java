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
 * File: ProteinFeatureType.java
 * Created by: dstaines
 * Created on: Nov 23, 2007
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.genomebuilder.model;

/**
 * Enumeration for protein feature types.
 * 
 * @author dstaines
 * 
 */
public enum ProteinFeatureType {

	SIGNAL_PEPTIDE("SIGNAL", "sig_peptide"), PRO_PEPTIDE("PROPEP", ""), PEPTIDE(
			"PEPTIDE", ""), CHAIN("CHAIN", "mat_peptide"), TRANSIT_PEPTIDE(
			"TRANSIT_PEPTIDE", "transit_peptide"), TRANSMEM("TRANSMEM", ""), GENE3D(
			"GENE3D", ""), PANTHER("PANTHER", ""), PFAM("PFAM", ""), PIRSF(
			"PIRSF", ""), PRINTS("PRINTS", ""), PROFILE("PFSCAN", ""), PROSITE(
			"SCANPROSITE", ""), SMART("SMART", ""), SSF("SUPERFAMILY", "SSF"), TIGRFAM(
			"TIGRFAMs", ""), PRODOM("PRODOM", ""), HAMAP("HAMAP", ""), CDD("CDD",""),
			SFLD("SFLD","");

	final String uniprotType;
	final String emblType;

	ProteinFeatureType(String uniprotType, String emblType) {
		this.uniprotType = uniprotType;
		this.emblType = emblType;
	}

	/**
	 * @return equivalent name used in as UNIPROT feature key
	 */
	public String getUniprotType() {
		return uniprotType;
	}

	/**
	 * @return equivalent name used in as EMBL feature key
	 */
	public String getEmblType() {
		return emblType;
	}

	public static ProteinFeatureType forInt(int i) {
		ProteinFeatureType type = null;
		for (ProteinFeatureType t : ProteinFeatureType.values()) {
			if (i == t.ordinal()) {
				type = t;
				break;
			}
		}
		return type;
	}

	public static ProteinFeatureType forUniprotString(String s) {
		ProteinFeatureType type = null;
		for (ProteinFeatureType t : ProteinFeatureType.values()) {
			if (s.equalsIgnoreCase(t.getUniprotType())) {
				type = t;
				break;
			}
		}
		return type;
	}

	public static ProteinFeatureType forEmblString(String s) {
		ProteinFeatureType type = null;
		for (ProteinFeatureType t : ProteinFeatureType.values()) {
			if (s.equalsIgnoreCase(t.getEmblType())) {
				type = t;
				break;
			}
		}
		return type;
	}

	public static ProteinFeatureType forString(String s) {
		ProteinFeatureType type = null;
		for (ProteinFeatureType t : ProteinFeatureType.values()) {
			if (s.equalsIgnoreCase(t.name()) || s.equalsIgnoreCase(t.getUniprotType())
					|| s.equalsIgnoreCase(t.getEmblType())) {
				type = t;
				break;
			}
		}
		return type;
	}

}
