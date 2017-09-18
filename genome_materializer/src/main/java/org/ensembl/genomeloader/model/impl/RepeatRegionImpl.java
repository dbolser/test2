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

package org.ensembl.genomeloader.model.impl;

import java.util.HashSet;
import java.util.Set;

import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.RepeatRegion;

/**
 * Created by IntelliJ IDEA. User: arnaud Date: 10-Jun-2008 Time: 17:17:05
 */
public class RepeatRegionImpl implements RepeatRegion {

	public static class RepeatUnitImpl implements RepeatUnit {

		private String repeatConsensus;

		private String repeatName;

		private String repeatClass;

		private String repeatType;

		public String getRepeatConsensus() {
			return repeatConsensus;
		}

		public String getRepeatName() {
			return repeatName;
		}

		public String getRepeatClass() {
			return repeatClass;
		}

		public String getRepeatType() {
			return repeatType;
		}

		public void setRepeatConsensus(String repeatConsensus) {
			this.repeatConsensus = repeatConsensus;
		}

		public void setRepeatName(String repeatName) {
			this.repeatName = repeatName;
		}

		public void setRepeatClass(String repeatClass) {
			this.repeatClass = repeatClass;
		}

		public void setRepeatType(String repeatType) {
			this.repeatType = repeatType;
		}

	}

	private EntityLocation location;

	private String analysis;

	private double score;

	private int repeatStart;

	private int repeatEnd;

	private RepeatUnit repeatUnit;

	private Set<DatabaseReference> databaseReferences;
	
	private String identifyingId;
	
	private String idString;

	public double getScore() {
		return score;
	}

	public int getRepeatStart() {
		return repeatStart;
	}

	public int getRepeatEnd() {
		return repeatEnd;
	}

	public RepeatUnit getRepeatUnit() {
		return repeatUnit;
	}

	public EntityLocation getLocation() {
		return location;
	}

	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public void setRepeatUnit(RepeatUnit repeatUnit) {
		this.repeatUnit = repeatUnit;
	}

	public void setRepeatStart(int repeatStart) {
		this.repeatStart = repeatStart;
	}

	public void setRepeatEnd(int repeatEnd) {
		this.repeatEnd = repeatEnd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.ensembl.genomeloader.genomebuilder.model.Gene#addDatabaseReference(uk.ac
	 * .ebi.proteome.genomebuilder.model.DatabaseReference)
	 */
	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ensembl.genomeloader.genomebuilder.model.Gene#getDatabaseReferences()
	 */
	public Set<DatabaseReference> getDatabaseReferences() {
		if (databaseReferences == null) {
			databaseReferences = new HashSet<DatabaseReference>();
		}
		return databaseReferences;
	}

	/**
	 * @param databaseReferences
	 *            the databaseReferences to set
	 */
	public void setDatabaseReferences(Set<DatabaseReference> databaseReferences) {
		this.databaseReferences = databaseReferences;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return identifyingId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.ensembl.genomeloader.genomebuilder.model.Identifiable#setIdentifyingId(java.lang.String)
	 */
	public void setIdentifyingId(String identifyingId) {
		this.identifyingId = identifyingId;
	}

	public String getIdString() {
		return this.idString;
	}
	
	public void setIdString(String id) {
		this.idString = id;
	}

	
}
