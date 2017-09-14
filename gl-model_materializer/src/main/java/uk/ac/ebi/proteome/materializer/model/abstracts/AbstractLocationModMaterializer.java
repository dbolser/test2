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
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocationModifier;
import uk.ac.ebi.proteome.materializer.model.mappers.LocationModifierMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.CollectionDbDataMaterializer;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.util.templating.TemplateBuilder;

/**
 * Used for materializing any type of location modifier used in this model for
 * the given component.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractLocationModMaterializer
	extends CollectionDbDataMaterializer<Persistable<EntityLocationModifier>, EntityLocation> {

	public static final String LOCM_SELECT =
		"l.id, lm.id, lm.type, lm.startat, lm.stopat, lm.seq, lm.aa";

	public AbstractLocationModMaterializer(Registry registry) {
		super(new LocationModifierMapRowMapper(), registry);
	}

	@Override
	public String getSqlStatement() {
		return TemplateBuilder.template(super.getSqlStatement(), "locm", LOCM_SELECT);
	}
}
