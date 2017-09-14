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
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.CollectionDbDataMaterializer;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.materializer.model.mappers.LocationMapRowMapper;
import uk.ac.ebi.proteome.util.templating.TemplateBuilder;

/**
 * Base class for loc materialization. Extended to allow underlying mechanisms
 * locate the correct Sql statement & the accepted query object.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractLocationMaterializer<Q>
	extends CollectionDbDataMaterializer<Persistable<EntityLocation>, Q> {

	public static final String LOC_SELECT = "l.id, l.min, l.max, l.strandn, l.state, 1, gc.is_circular, gc.length, l.min_fuzzy, l.max_fuzzy";

	public AbstractLocationMaterializer(Registry registry) {
		super(new LocationMapRowMapper(), registry);
	}

	@Override
	public String getSqlStatement() {
		return TemplateBuilder.template(super.getSqlStatement(), "loc", LOC_SELECT);
	}
}
