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

package org.ensembl.genomeloader.genomebuilder.model;

import java.util.List;

import org.biojavax.bio.seq.RichLocation;

/**
 * Extension of RichLocation for use with genomebuilder model which adds a
 * mapping state flag and modifiers
 *
 * @author dstaines
 *
 */
public interface EntityLocation extends RichLocation, Integr8ModelComponent {

	/**
	 * Enumeration describing possible state of location
	 *
	 * @author dstaines
	 */
	public enum MappingState {

		ANNOTATED(0, "Annotated location in agreement with protein"), ANNOTATED_DISAGREEMENT(
				1, "Annotated location not in agreement with protein"), MAPPED(
				2, "Mapped location"), MAPPED_FAILURE(3,
				"Annotated location not in agreement with protein but not mappable");
				//COMPOSITE(4,"Composite of subsiduary location");

		final int intValue;
		final String description;

		MappingState(int intValue, String description) {
			this.intValue = intValue;
			this.description = description;
		}

		public int getIntValue() {
			return intValue;
		}

		public String getDescription() {
			return description;
		}

		public static MappingState valueOf(int i) {
			MappingState loc = null;
			for (MappingState l : MappingState.values()) {
				if (l.intValue == i) {
					loc = l;
					break;
				}
			}
			return loc;
		}

		public static MappingState findConsensus(MappingState state1,
				MappingState state2) {
			MappingState consensus = null;
			for (int i = MappingState.values().length - 1; i >= 0; i--) {
				consensus = MappingState.values()[i];
				if (state1 == consensus || state2 == consensus) {
					break;
				}
			}
			return consensus;
		}

	}

	/**
	 * @return mapping state of location
	 */
	public abstract MappingState getState();

	public abstract void setState(MappingState state);

	/**
	 * @return list of insertions in protein sequence encoded by location
	 */
	public abstract List<EntityLocationInsertion> getInsertions();

	public abstract void addInsertion(EntityLocationInsertion insertion);

	/**
	 * @return list of exceptions to translated protein sequence
	 */
	public abstract List<EntityLocationException> getExceptions();

	public abstract void addException(EntityLocationException exception);

}
