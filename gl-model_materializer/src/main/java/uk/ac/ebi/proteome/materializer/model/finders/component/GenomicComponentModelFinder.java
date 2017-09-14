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

package uk.ac.ebi.proteome.materializer.model.finders.component;

import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.resolver.DataItem;

import java.util.Collection;

/**
 * Attempts to fully populate a genomic component with all available
 * information from the genomebuilder model schema
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class GenomicComponentModelFinder implements Finder<Persistable<GenomicComponent>, Persistable<GenomicComponent>> {

	private final MaterializedDataInstance<Collection<DataItem>, GenomicComponent> dataItemMdi;

	public GenomicComponentModelFinder(
		MaterializedDataInstance<Collection<DataItem>, GenomicComponent> dataItemMdi) {
		this.dataItemMdi = dataItemMdi;
	}

	public Persistable<GenomicComponent> find(Persistable<GenomicComponent> query) {
		GenomicComponent component = query.getPersistableObject();
		for(DataItem data: dataItemMdi.getData(query)) {
			component.getMetaData().addDataItem(data);
		}
		return query;
	}
}
