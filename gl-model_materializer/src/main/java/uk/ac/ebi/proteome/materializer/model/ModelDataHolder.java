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

import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.resolver.DataItem;

/**
 * Used as a way of holding all the materialized data together which is
 * required for materializing a complete model. If you are going to use
 * {@link ModelFinder} it is recommended that you must use this class
 * to provide the data (therefore it is up to you to provide the necessary
 * conversion/population code).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ModelDataHolder {

	public MaterializedDataInstance<Persistable<Genome>, String> genome;

	public MaterializedDataInstance<Collection<Persistable<GenomicComponent>>, Genome> components;
	
	public MaterializedDataInstance<Collection<Persistable<Gene>>, GenomicComponent>
		componentToGene;

	public MaterializedDataInstance<Collection<DataItem>, GenomicComponent>
		componentToDataItem;

	public MaterializedDataInstance<Collection<Persistable<GeneName>>, Gene>
		geneToName;
	
	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, GeneName>
		geneNameToXref;

	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Gene>
		geneToXref;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Gene>
		geneToLocation;

	public MaterializedDataInstance<Collection<Persistable<Pseudogene>>, GenomicComponent>
		componentToPseudogene;

	public MaterializedDataInstance<Collection<Persistable<GeneName>>, Pseudogene>
		pseudogeneToName;
	
	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, GeneName>
		pseudogeneNameToXref;

	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Pseudogene>
		pseudogeneToXref;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Pseudogene>
		pseudogeneToLocation;

	public MaterializedDataInstance<Collection<Persistable<Protein>>, Gene>
		geneToProtein;

	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Protein>
		proteinToXref;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Protein>
		proteinToLocation;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, EntityLocation>
		proteinLocationToSubLocation;

	public MaterializedDataInstance<Collection<Persistable<EntityLocationModifier>>, EntityLocation>
		proteinLocationToProteinLocationMod;

	public MaterializedDataInstance<Collection<Persistable<ProteinFeature>>, Protein>
		proteinToProteinFeature;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, ProteinFeature>
		proteinFeatureToLocation;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, EntityLocation>
		proteinFeatureLocationToSubLocation;

	public MaterializedDataInstance<Collection<Persistable<EntityLocationModifier>>, EntityLocation>
		proteinFeatureLocationToProteinLocationMod;

	public MaterializedDataInstance<Collection<Persistable<Transcript>>, Protein>
		proteinToTranscript;

	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Transcript>
		transcriptToXref;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Transcript>
		transcriptToLocation;

	public MaterializedDataInstance<Collection<Persistable<Operon>>, Transcript>
		transcriptToOperon;

	public MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Operon>
		operonToLocation;

	public MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Operon>
		operonToXref;
}
