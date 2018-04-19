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

package org.ensembl.genomeloader.materializer.processors;

import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaXmlRetriever;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;

public class LocationIdEnaGenomeProcessor extends EnaGenomeProcessor {

    public LocationIdEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv) {
        this(config, srv, new XmlDatabaseReferenceTypeRegistry());
    }

    public LocationIdEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv,
            DatabaseReferenceTypeRegistry registry) {
        super(config, srv, registry, new EnaXmlRetriever(config.getEnaEntryUrl()));
        this.addProcessor(new LocationIdProcessor(registry));
    }

}
