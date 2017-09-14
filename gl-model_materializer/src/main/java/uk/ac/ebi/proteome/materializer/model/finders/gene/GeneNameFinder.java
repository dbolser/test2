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
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.materializer.model.finders.abstracts.AbstractGeneNameFinder;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class GeneNameFinder extends AbstractGeneNameFinder<Gene> {
	public GeneNameFinder(MaterializedDataInstance<Collection<Persistable<GeneName>>, Gene> geneNameMdi, 
			Finder<Persistable<GeneName>, Collection<DatabaseReference>> geneNameXrefFinder) {
		super(geneNameMdi, geneNameXrefFinder);
	}
}
