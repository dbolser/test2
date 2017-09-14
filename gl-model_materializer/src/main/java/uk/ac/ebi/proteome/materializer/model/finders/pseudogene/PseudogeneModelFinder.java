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

package uk.ac.ebi.proteome.materializer.model.finders.pseudogene;

import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.gene.GeneModelFinder;
import uk.ac.ebi.proteome.materializer.model.finders.generic.GeneNameXrefFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * Very similar setup to the {@link GeneModelFinder} but using {@link Pseudogene}
 * as the base object to bring back.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class PseudogeneModelFinder extends AbstractModelFinder<Pseudogene, GenomicComponent> {

	public PseudogeneModelFinder(
		MaterializedDataInstance<Collection<Persistable<Pseudogene>>, GenomicComponent> pseudogeneMdi,
		MaterializedDataInstance<Collection<Persistable<EntityLocation>>, Pseudogene> locationMdi,
		MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Pseudogene> xrefMdi,
		MaterializedDataInstance<Collection<Persistable<GeneName>>, Pseudogene> geneNameMdi,
		MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, GeneName> geneNameXrefMdi) {

		super(pseudogeneMdi);
		this.addPostProcessor(new LocationPostProcessor<Pseudogene>(new PseudogeneLocationFinder(locationMdi)));
		this.addPostProcessor(new XrefPostProcessor<Pseudogene>(new PseudogeneXrefFinder(xrefMdi)));
		this.addPostProcessor(
			new GeneNamePostProcessor<Pseudogene>(
				new PseudogeneNameFinder(geneNameMdi,
					new GeneNameXrefFinder(geneNameXrefMdi))
			)
		);
	}
}
