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

package uk.ac.ebi.proteome.genomebuilder.model.impl;

import org.biojavax.bio.seq.RichLocation;

import uk.ac.ebi.proteome.genomebuilder.model.AssemblySequence;

public class AssemblySequenceImpl implements AssemblySequence {

	private static final long serialVersionUID = 1L;
	private RichLocation location;
	private String accession;
	private int version;
	private int start;
	private int end;

	public AssemblySequenceImpl(RichLocation location, String accession,
			int version, int start, int end) {
		super();
		this.location = location;
		this.accession = accession;
		this.version = version;
		this.start = start;
		this.end = end;
	}

	public RichLocation getLocation() {
		return location;
	}

	public void setLocation(RichLocation location) {
		this.location = location;
	}

	public String getAccession() {
		return accession;
	}

	public void setAccession(String accession) {
		this.accession = accession;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getIdString() {
		return getAccession() + "." + getVersion() + ":" + getStart()
				+ "-" + getEnd();
	}

}
