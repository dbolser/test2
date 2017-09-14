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

package uk.ac.ebi.proteome.materializer.model.abstracts;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.materializer.model.mappers.SubLocationMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.CollectionDbDataMaterializer;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.util.templating.TemplateBuilder;

/**
 * Base class for sub-loc materialization. Extended to allow underlying
 * mechanisms locate the correct Sql statement & the accepted query object.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class AbstractSubLocationMaterializer
	extends CollectionDbDataMaterializer<Persistable<EntityLocation>, EntityLocation> {

	public static final String SLOC_SELECT =
		"l.id, sl.id, sl.min, sl.max, sl.strandn, l.state, sl.rank, gc.is_circular, gc.length, sl.min_fuzzy, sl.max_fuzzy";

	public static final String SLOC_ORDERBY = "order by l.id, sl.rank";

	public AbstractSubLocationMaterializer(Registry registry) {
		super(new SubLocationMapRowMapper(), registry);
	}

	@Override
	public String getSqlStatement() {
		return TemplateBuilder.template(
			super.getSqlStatement(), "sloc", SLOC_SELECT, "sloc-o", SLOC_ORDERBY);
	}
}
