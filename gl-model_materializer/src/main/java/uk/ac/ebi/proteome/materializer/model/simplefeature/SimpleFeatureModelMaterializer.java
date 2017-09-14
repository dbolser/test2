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

package uk.ac.ebi.proteome.materializer.model.simplefeature;

import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.SimpleFeature;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.CollectionDbDataMaterializer;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.materializer.model.mappers.SimpleFeatureModelMapRowMapper;

/**
 * Created by IntelliJ IDEA.
 * User: arnaud
 * Date: 11-Jul-2008
 * Time: 17:15:47
 */
public class SimpleFeatureModelMaterializer
        extends CollectionDbDataMaterializer<Persistable<SimpleFeature>, GenomicComponent> {

    public SimpleFeatureModelMaterializer(Registry registry) {
		super(new SimpleFeatureModelMapRowMapper(), registry);
    }

}
