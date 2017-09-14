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

package uk.ac.ebi.proteome.materializer.model.finders.abstracts;

import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;

import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.materializer.model.finders.generic.MockFinders;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractGeneNameFinder<M> implements Finder<Persistable<M>, Collection<GeneName>> {

	private final MaterializedDataInstance<Collection<Persistable<GeneName>>, M> mdi;
	private final Finder<Persistable<GeneName>, Collection<DatabaseReference>> geneNameXrefFinder;

	public AbstractGeneNameFinder(MaterializedDataInstance<Collection<Persistable<GeneName>>, M> mdi) {
		this.mdi = mdi;
		this.geneNameXrefFinder = MockFinders.createCollection();
	}
	
	public AbstractGeneNameFinder(MaterializedDataInstance<Collection<Persistable<GeneName>>, M> mdi,
			Finder<Persistable<GeneName>, Collection<DatabaseReference>> geneNameXrefFinder) {
		this.mdi = mdi;
		this.geneNameXrefFinder = geneNameXrefFinder;
	}

	public Collection<GeneName> find(Persistable<M> query) {
		Collection<GeneName> names = createArrayList();
		for(Persistable<GeneName> geneNamePersistable: mdi.getData(query)) {
			findAndProcessGeneNameXrefs(geneNamePersistable);
			names.add(geneNamePersistable.getPersistableObject());
		}
		return names;
	}
	
	private void findAndProcessGeneNameXrefs(Persistable<GeneName> persistable) {
		GeneName name = persistable.getPersistableObject();
		Collection<DatabaseReference> xrefs = geneNameXrefFinder.find(persistable);
		if(!xrefs.isEmpty()) {
			for(DatabaseReference xref: xrefs) {
				name.addDatabaseReference(xref);
			}
		}
	}
}
