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

import java.util.concurrent.Executor;

import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.services.sql.SqlService;

public class IdMappingEnaGenomeProcessor extends EnaGenomeProcessor {

    public IdMappingEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv) {
        this(config, srv, new XmlDatabaseReferenceTypeRegistry(), new SimpleExecutor());
    }

    public IdMappingEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv, Executor executor) {
        this(config, srv, new XmlDatabaseReferenceTypeRegistry(), executor);
    }

    public IdMappingEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry) {
        this(config, srv, registry, new SimpleExecutor());
    }

    public IdMappingEnaGenomeProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry,
            Executor executor) {
        super(config, srv, registry, executor);
        this.addProcessor(new TypeAwareDuplicateIdProcessor(config));
        this.addProcessor(new IdMappingProcessor(config, srv));
    }

}
