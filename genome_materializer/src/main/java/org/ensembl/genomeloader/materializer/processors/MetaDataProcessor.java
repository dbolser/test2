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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.model.Genome;

/**
 * {@link GenomeProcessor} to set additional metadata needed for the load
 * 
 * @author dstaines
 * 
 */
public class MetaDataProcessor implements GenomeProcessor {

    private static final String ENA = "ENA";
    private static final String ENA_URL = "http://www.ebi.ac.uk/ena/data/view/";
    private static final String ENA_NAME = "European Nucleotide Archive";
    public static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM");
    private Log log;

    protected Log getLog() {
        if (log == null)
            log = LogFactory.getLog(this.getClass());
        return log;
    }

    protected final EnaGenomeConfig config;

    public MetaDataProcessor(EnaGenomeConfig config) {
        this.config = config;
    }

    public void processGenome(Genome genome) {
        GenomeMetaData md = genome.getMetaData();
        md.setProductionName(md.getName().toLowerCase().replaceAll("[^a-z0-9_]+", "_").replaceAll("[_]+", "_"));
        md.setGenebuild(FORMATTER.format(md.getUpdateDate())+ENA);
        md.setProvider(ENA_NAME);
        md.setProviderUrl(ENA_URL + md.getId());        
    }

}
