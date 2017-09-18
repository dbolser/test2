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

package org.ensembl.genomeloader.materializer.identifiers;

import org.ensembl.genomeloader.model.AnnotatedGene;
import org.ensembl.genomeloader.model.Identifiable;
import org.ensembl.genomeloader.model.Protein;
import org.ensembl.genomeloader.model.RepeatRegion;
import org.ensembl.genomeloader.model.RnaTranscript;
import org.ensembl.genomeloader.model.Transcript;

/**
 * Interface defining how an ENAGenomes identifier can be produced given a
 * placeholder identifier
 * 
 * @author dstaines
 * 
 */
public interface EnaIdentifierMapper {

	public static final String ENA_ID_STEM = "EB";
	
	@SuppressWarnings("rawtypes")
	public static enum IdentifierType {
		
		GENE("G", "gene", new Class[] { AnnotatedGene.class }), 
		TRANSCRIPT("T","transcript", new Class[] { Transcript.class, RnaTranscript.class }), 
		TRANSLATION("P","translation",new Class[] { Protein.class }), 
		REPEAT("R","repeat_region",new Class[] { RepeatRegion.class }), 
		EXON("E", "exon",new Class[0]);
		
		private final String initial;
		private final String table;
		private final Class[] classes;

		IdentifierType(String initial, String table, Class[] classes) {
			this.initial = initial;
			this.classes = classes;
			this.table = table;
		}

		public String getInitial() {
			return initial;
		}
		public String getTable() {
			return table;
		}
		
		@SuppressWarnings("unchecked")
		public static IdentifierType typeForClass(
				Class<? extends Identifiable> clazz) {
			IdentifierType type = null;
			for (IdentifierType t : IdentifierType.values()) {
				for (Class c : t.classes) {
					if (c.isAssignableFrom(clazz)) {
						type = t;
						break;
					}
				}
			}
			return type;
		}

	}

	/**
	 * Return an ENAGenomes identifier for the entity with the corresponding
	 * ENA-style identifier
	 * 
	 * @param type
	 * @param identifier
	 * @return ENAGenomes identifier
	 */
	public abstract String mapIdentifier(IdentifierType type, String identifier);

}
