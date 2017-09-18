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

package org.ensembl.genomeloader.genomebuilder.model.impl;

import org.biojavax.bio.seq.RichLocation;
import org.ensembl.genomeloader.genomebuilder.model.AssemblyGap;

public class AssemblyGapImpl implements AssemblyGap {

	private RichLocation location;
	private int length;
	private boolean unknownLength;

	public AssemblyGapImpl(RichLocation location, int length,
			boolean unknownLength) {
		super();
		this.location = location;
		this.length = length;
		this.unknownLength = unknownLength;
	}

	public RichLocation getLocation() {
		return location;
	}

	public void setLocation(RichLocation location) {
		this.location = location;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isUnknownLength() {
		return unknownLength;
	}

	public void setUnknownLength(boolean unknownLength) {
		this.unknownLength = unknownLength;
	}

	public String getIdString() {
		return "gap:"+getLocation().toString();
	}

}
