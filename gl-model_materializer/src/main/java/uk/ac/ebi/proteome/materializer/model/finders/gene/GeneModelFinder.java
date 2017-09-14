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

package uk.ac.ebi.proteome.materializer.model.finders.gene;

import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.generic.GeneNameXrefFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * A finder which will fully reconstruct a gene object (so you will have
 * a Gene object which has all its links in place).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class GeneModelFinder extends AbstractModelFinder<Gene, GenomicComponent> {

	public GeneModelFinder(
		MaterializedDataInstance<Collection<Persistable<Gene>>, GenomicComponent> geneMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Gene> locationMdi,
		MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Gene> xrefMdi,
		MaterializedDataInstance<Collection<Persistable<GeneName>>, Gene> geneNameMdi,
		MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, GeneName> geneNameXrefMdi) {

		super(geneMdi);
		addPostProcessor(new LocationPostProcessor<Gene>(new GeneLocationFinder(locationMdi)));
		addPostProcessor(new XrefPostProcessor<Gene>(new GeneXrefFinder(xrefMdi)));
		addPostProcessor(
			new GeneNamePostProcessor<Gene>(new GeneNameFinder(geneNameMdi, 
					new GeneNameXrefFinder(geneNameXrefMdi)))
		);
	}
}
