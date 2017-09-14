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
 * File: GenomicComponentDumper.java
 * Created by: dstaines
 * Created on: Dec 2, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.materializer.GenomeMaterializer;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.ProteinFeature;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.TableBackedDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.model.finders.gene.GeneModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.operon.OperonModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.protein.ProteinModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.proteinfeature.ProteinFeatureModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.pseudogene.PseudogeneModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.transcript.TranscriptModelFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.services.ServiceContext;

/**
 * @author dstaines
 * 
 */
public class DbBackedGenomeMaterializer implements GenomeMaterializer {

	private final ModelRegistry modelReg;
	private final ModelMaterializer modMat;

	public DbBackedGenomeMaterializer(ModelRegistry reg,
			DatabaseReferenceTypeRegistry typeReg) {
		modelReg = reg;
		this.modelReg.put(DatabaseReferenceTypeRegistry.class, typeReg);
		modMat = ModelMaterializer.getModelMaterializer(reg);
	}

	public DbBackedGenomeMaterializer(ServiceContext context, String uri) {
		this(new ModelRegistry(uri, context),
				new TableBackedDatabaseReferenceTypeRegistry(context, uri));
	}

	private Log log;

	protected Log getLog() {
		if (log == null)
			log = LogFactory.getLog(this.getClass());
		return log;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeuk.ac.ebi.proteome.materializer.model.GenomicComponentMaterializer#
	 * getComponent
	 * (uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData)
	 */
	public Genome getGenome(GenomeInfo md) {
		return getGenome(md.getId());
	}
	public Genome getGenome(String ac) {
		return getGenome(Long.valueOf(ac));
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @seeuk.ac.ebi.proteome.materializer.model.GenomicComponentMaterializer#
	 * getComponent(java.lang.String)
	 */
	public Genome getGenome(Long ac) {

		getLog().debug("Materializing data for " + ac);
		ModelDataHolder data = modMat.getData(ac);
		Persistable<Genome> pg = data.genome.getData();
		getLog().debug("Getting components");
		Genome genome = pg.getPersistableObject();
		for (Persistable<GenomicComponent> gc : data.components.getData(pg)) {

			GenomicComponent component = gc.getPersistableObject();

			getLog().debug("Building gene finder");
			GeneModelFinder geneFinder = new GeneModelFinder(
					data.componentToGene, data.geneToLocation, data.geneToXref,
					data.geneToName, data.geneNameToXref);

			getLog().debug("Building protein finder");
			ProteinModelFinder proteinFinder = new ProteinModelFinder(
					data.geneToProtein, data.proteinToLocation,
					data.proteinLocationToSubLocation,
					data.proteinLocationToProteinLocationMod,
					data.proteinToXref);

			getLog().debug("Building protein feature finder");
			ProteinFeatureModelFinder featureFinder = new ProteinFeatureModelFinder(
					data.proteinToProteinFeature,
					data.proteinFeatureToLocation,
					data.proteinFeatureLocationToSubLocation,
					data.proteinFeatureLocationToProteinLocationMod);

			// pgene finder
			getLog().debug("Building pseudogene finder");
			PseudogeneModelFinder pgeneFinder = new PseudogeneModelFinder(
					data.componentToPseudogene, data.pseudogeneToLocation,
					data.pseudogeneToXref, data.pseudogeneToName,
					data.pseudogeneNameToXref);

			// transcript finder
			getLog().debug("Building transcript finder");
			TranscriptModelFinder transcriptFinder = new TranscriptModelFinder(
					data.proteinToTranscript, data.transcriptToLocation,
					data.transcriptToXref);

			// operon finder
			getLog().debug("Building operon finder");
			OperonModelFinder operonFinder = new OperonModelFinder(
					data.transcriptToOperon, data.operonToLocation,
					data.operonToXref);
			getLog().debug("Constructing gene model");
			for (Persistable<Gene> gene : geneFinder.find(gc)) {
				for (Persistable<Protein> protein : proteinFinder.find(gene)) {
					for (Persistable<Transcript> transcript : transcriptFinder
							.find(protein)) {
						for (Persistable<Operon> operon : operonFinder
								.find(transcript)) {
							transcript.getPersistableObject().setOperon(
									operon.getPersistableObject());
						}
						for (Persistable<ProteinFeature> feature : featureFinder
								.find(protein)) {
							protein.getPersistableObject().addProteinFeature(
									feature.getPersistableObject());
						}
						transcript.getPersistableObject().addProtein(
								protein.getPersistableObject());
						protein.getPersistableObject().addTranscript(
								transcript.getPersistableObject());
					}
					gene.getPersistableObject().addProtein(
							protein.getPersistableObject());
				}
				component.getGenes().add(gene.getPersistableObject());
			}
			getLog().debug("Constructing pseudogene model");
			for (Persistable<Pseudogene> pgene : pgeneFinder.find(gc)) {
				component.addPseudogene(pgene.getPersistableObject());
			}
			genome.addGenomicComponent(component);
		}
		return genome;
	}
}
