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

package uk.ac.ebi.proteome.materializer.model;

import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.materializer.model.finders.component.GenomicComponentModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.gene.GeneModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.operon.OperonModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.protein.ProteinModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.proteinfeature.ProteinFeatureModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.pseudogene.PseudogeneModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.transcript.TranscriptModelFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * Top level finder which uses all other model based finders to fully populate
 * a component from numerous {@link MaterializedDataInstance} objects. Unlike
 * the other finders it assumes that you have been able to get an instance
 * of {@link GenomicComponent} and it is from this object you wish to build
 * the model for (and all elements used in its construction are related to
 * said component).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ModelFinder implements Finder<Persistable<GenomicComponent>, GenomicComponent> {

	private final ModelDataHolder data;

	public ModelFinder(ModelDataHolder data) {
		this.data = data;
	}

	public GenomicComponent find(Persistable<GenomicComponent> query) {
		GenomicComponent component = query.getPersistableObject();

		processComponent(query);

		GeneModelFinder geneFinder = new GeneModelFinder(data.componentToGene,
			data.geneToLocation, data.geneToXref, data.geneToName, data.geneNameToXref);
		for(Persistable<Gene> genePersistable: geneFinder.find(query)) {
			processGene(genePersistable);
			component.addGene(genePersistable.getPersistableObject());
		}

		PseudogeneModelFinder pseudogeneFinder = new PseudogeneModelFinder(
			data.componentToPseudogene, data.pseudogeneToLocation,
			data.pseudogeneToXref, data.pseudogeneToName, data.pseudogeneNameToXref
		);
		for(Persistable<Pseudogene> pseudogenePersistable: pseudogeneFinder.find(query)) {
			component.addPseudogene(pseudogenePersistable.getPersistableObject());
		}

		return component;
	}

	/**
	 * Will force the addition of the data items to a component
	 */
	protected void processComponent(Persistable<GenomicComponent> query) {
		new GenomicComponentModelFinder(data.componentToDataItem).find(query);
	}

	/**
	 * Triggers off protein code
	 */
	protected void processGene(Persistable<Gene> query) {
		Gene gene = query.getPersistableObject();

		ProteinModelFinder finder = new ProteinModelFinder(data.geneToProtein,
			data.proteinToLocation, data.proteinLocationToSubLocation,
			data.proteinLocationToProteinLocationMod, data.proteinToXref);
		for(Persistable<Protein> proteinPersistable: finder.find(query)) {
			processProtein(proteinPersistable);
			gene.addProtein(proteinPersistable.getPersistableObject());
		}
	}

	/**
	 * Triggers protein feature & transcript code
	 */
	protected void processProtein(Persistable<Protein> query) {
		Protein protein = query.getPersistableObject();

		ProteinFeatureModelFinder featureFinder = new ProteinFeatureModelFinder(
			data.proteinToProteinFeature,
			data.proteinFeatureToLocation,
			data.proteinFeatureLocationToSubLocation,
			data.proteinFeatureLocationToProteinLocationMod
		);
		for(Persistable<ProteinFeature> featurePersistable: featureFinder.find(query)) {
			protein.addProteinFeature(featurePersistable.getPersistableObject());
		}

		TranscriptModelFinder transcriptFinder = new TranscriptModelFinder(
			data.proteinToTranscript,
			data.transcriptToLocation,
			data.transcriptToXref
		);
		for(Persistable<Transcript> transcriptPersistable: transcriptFinder.find(query)) {
			processTranscript(transcriptPersistable);
			protein.addTranscript(transcriptPersistable.getPersistableObject());
		}
	}

	/**
	 * Triggers Operon code
	 */
	protected void processTranscript(Persistable<Transcript> query) {
		Transcript transcript = query.getPersistableObject();

		OperonModelFinder finder = new OperonModelFinder(
			data.transcriptToOperon,
			data.operonToLocation,
			data.operonToXref
		);
		for(Persistable<Operon> operonPersistable: finder.find(query)) {
			transcript.setOperon(operonPersistable.getPersistableObject());
		}
	}
}
