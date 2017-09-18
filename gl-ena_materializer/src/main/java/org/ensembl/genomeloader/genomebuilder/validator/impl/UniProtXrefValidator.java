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

package org.ensembl.genomeloader.genomebuilder.validator.impl;

import java.util.Collection;

import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType;
import org.ensembl.genomeloader.genomebuilder.model.Gene;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;
import org.ensembl.genomeloader.genomebuilder.model.ModelUtils;
import org.ensembl.genomeloader.genomebuilder.model.Protein;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidationException;
import org.ensembl.genomeloader.genomebuilder.validator.GenomeValidator;
import org.ensembl.genomeloader.genomebuilder.validator.UniprotReferenceValidationException;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;

public class UniProtXrefValidator implements GenomeValidator {

	private final Collection<DatabaseReferenceType> types;

	public UniProtXrefValidator(DatabaseReferenceTypeRegistry reg) {
		this(reg.getTypesForName("UniProtKB"));
	}

	public UniProtXrefValidator(Collection<DatabaseReferenceType> types) {
		this.types = types;
	}

	public void validateGenome(Genome genome) throws GenomeValidationException {
		for (GenomicComponent gc : genome.getGenomicComponents()) {
			for (Gene g : gc.getGenes()) {
				for (Protein p : g.getProteins()) {
					if (!p.isPseudo()) {
						boolean isFound = false;
						for (DatabaseReferenceType type : types) {
							if (!ModelUtils.getReferencesForType(p, type)
									.isEmpty()) {
								isFound = true;
								break;
							}
						}
						if (!isFound) {
							throw new UniprotReferenceValidationException(
									"Protein " + p.getIdentifyingId()
											+ " has no UniProt xrefs");
						}
					}
				}
			}
		}
	}

}
