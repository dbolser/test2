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

import org.apache.commons.lang.StringUtils;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;

/**
 * Delegating processor which will decorate a genome with UPI xrefs and then
 * Interpro domains, GO terms and UniProt xrefs
 * 
 * @author dstaines
 * 
 */
public class EnaGenomeProcessor extends DelegatingGenomeProcessor {

    public EnaGenomeProcessor(EnaGenomeConfig config, SqlService srv) {
        this(config, srv, new XmlDatabaseReferenceTypeRegistry(), new SimpleExecutor());
    }

    public EnaGenomeProcessor(EnaGenomeConfig config, SqlService srv, Executor executor) {
        this(config, srv, new XmlDatabaseReferenceTypeRegistry(), executor);
    }

    public EnaGenomeProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry,
            Executor executor) {

        super(new ComponentSortingProcessor(config), new PubMedCentralProcessor(registry),
                new LocationOverlapProcessor(config, registry), new LocusTagMergeProcessor(config, registry),
                new AltTranslationProcessor(config, registry), new AssemblyContigProcessor(config, registry, executor),
                new MetaDataProcessor(config));

        if (!StringUtils.isEmpty(config.getEnaUri())) {
            addProcessor(new ConXrefProcessor(config, srv, registry));
        }

        if (!StringUtils.isEmpty(config.getUniparcUri())) {
            addProcessor(new UpiGenomeProcessor(config, srv, registry));
        }

        if (!StringUtils.isEmpty(config.getUniProtUri())) {
            addProcessor(new UniProtDescriptionGenomeProcessor(config, srv, registry));
            addProcessor(new UniProtXrefGenomeProcessor(config, srv, registry));
            addProcessor(new UniProtECGenomeProcessor(config, srv, registry));
        }

        if (!StringUtils.isEmpty(config.getInterproUri())) {
            addProcessor(new UpiInterproGenomeProcessor(config, srv, registry));
            addProcessor(new InterproPathwayGenomeProcessor(config, srv, registry));
        }

        if (!StringUtils.isEmpty(config.getRfamUri())) {
            addProcessor(new RfamProcessor(config, srv, registry));
        }

        if (config.isUseAccessionsForNames()) {
            addProcessor(new ComponentAccessionNamingProcessor());
        }

        if (!config.isLoadTrackingReferences()) {
            addProcessor(new TrackingRefRemovalProcessor());
        }

    }

}
