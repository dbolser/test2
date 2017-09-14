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
 * File: MergingRnageneModelMaterializer.java
 * Created by: dstaines
 * Created on: Nov 5, 2009
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model.rnagene;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.collections.DefaultingMap;

/**
 * @author dstaines
 *
 */
public class MergingRnageneModelMaterializer implements
		DataMaterializer<Collection<Persistable<Rnagene>>, GenomicComponent> {

	protected final List<DataMaterializer<Collection<Persistable<Rnagene>>, GenomicComponent>> materializers;

	public MergingRnageneModelMaterializer(
			List<DataMaterializer<Collection<Persistable<Rnagene>>, GenomicComponent>> materializers) {
		this.materializers = materializers;
	}

	public MaterializedDataInstance<Collection<Persistable<Rnagene>>, GenomicComponent> getMaterializedDataInstance(
			Object... args) {
		Map<Object, Collection<Persistable<Rnagene>>> results = CollectionUtils
				.createHashMap();
		for (DataMaterializer<Collection<Persistable<Rnagene>>, GenomicComponent> mat : materializers) {
			MaterializedDataInstance<Collection<Persistable<Rnagene>>, GenomicComponent> mpi = mat
					.getMaterializedDataInstance(args);
			mergeResults(results, mpi);
		}

		MaterializedDataInstance<Collection<Persistable<Rnagene>>, GenomicComponent> instance = new MaterializedDataInstance<Collection<Persistable<Rnagene>>, GenomicComponent>(
				new DefaultingMap<Object, Collection<Persistable<Rnagene>>>(
						results, Collections.EMPTY_LIST));

		return instance;
	}

	protected void mergeResults(
			Map<Object, Collection<Persistable<Rnagene>>> results,
			MaterializedDataInstance<Collection<Persistable<Rnagene>>, GenomicComponent> mpi) {
		for (Entry<Object, Collection<Persistable<Rnagene>>> e : mpi.getMap()
				.entrySet()) {
			if(results.containsKey(e.getKey()))
				mergeGenes(results.get(e.getKey()), e.getValue());
			else
				results.put(e.getKey(),e.getValue());
		}
	}

	protected void mergeGenes(Collection<Persistable<Rnagene>> existingGenes,
			Collection<Persistable<Rnagene>> newGenes) {
		for (Persistable<Rnagene> newGene : newGenes) {
			if (!hasGene(newGene, existingGenes)) {
				existingGenes.add(newGene);
			}
		}
	}

	protected boolean hasGene(Persistable<Rnagene> newGene,
			Collection<Persistable<Rnagene>> existingGenes) {
		boolean found = false;
		for (Persistable<Rnagene> gene : existingGenes) {
			if (LocationUtils.outerOverlaps(gene.getPersistableObject()
					.getLocation(), newGene.getPersistableObject()
					.getLocation())) {
				mergeGenePair(gene, newGene);
				break;
			}
		}
		return found;
	}

	protected void mergeGenePair(Persistable<Rnagene> gene,
			Persistable<Rnagene> newGene) {
		if (LocationUtils.encloses(
				newGene.getPersistableObject().getLocation(), gene
						.getPersistableObject().getLocation())) {
			gene.getPersistableObject().setLocation(
					newGene.getPersistableObject().getLocation());
		}
		gene.getPersistableObject().getDatabaseReferences().addAll(
				newGene.getPersistableObject().getDatabaseReferences());
		// ?also override the name etc. if not set?
	}

}
