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

package org.ensembl.genomeloader.materializer;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.materializer.impl.XmlEnaComponentParser;
import org.ensembl.genomeloader.materializer.processors.GenomeProcessor;
import org.ensembl.genomeloader.materializer.processors.LocationIdEnaGenomeProcessor;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.model.impl.GenomeImpl;
import org.ensembl.genomeloader.model.impl.GenomicComponentImpl;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.util.collections.CollectionUtils;
import org.ensembl.genomeloader.util.templating.TemplateBuilder;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;

/**
 * Merge
 * 
 * @author dstaines
 * 
 */
public class EnaGenomeMaterializer {

    public final static DateFormat ENA_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static Date parseEnaDate(String dateStr) {
        try {
            return ENA_DATEFORMAT.parse(dateStr);
        } catch (ParseException e) {
            throw new EnaParsingException("Could not parse date " + dateStr, e);
        }
    }

    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    private final String enaXmlLoc;
    private final EnaParser<GenomicComponentImpl> parser;
    private final GenomeProcessor processor;

    public EnaGenomeMaterializer(EnaGenomeConfig config) {
        this(config, new LocalSqlService(),new SimpleExecutor());
    }

    public EnaGenomeMaterializer(EnaGenomeConfig config, SqlService srv, Executor executor) {
        this(config, new XmlEnaComponentParser(executor, new XmlDatabaseReferenceTypeRegistry()),
                new LocationIdEnaGenomeProcessor(config, srv));
    }

    public EnaGenomeMaterializer(EnaGenomeConfig config, GenomeProcessor processor) {
        this(config, processor, new SimpleExecutor());
    }

    public EnaGenomeMaterializer(EnaGenomeConfig config, GenomeProcessor processor, Executor executor) {
        this(config, new XmlEnaComponentParser(executor, new XmlDatabaseReferenceTypeRegistry()), processor);
    }

    public EnaGenomeMaterializer(EnaGenomeConfig config, EnaParser<GenomicComponentImpl> parser) {
        this(config.getEnaXmlUrl(), parser, null);
    }

    public EnaGenomeMaterializer(EnaGenomeConfig config, EnaParser<GenomicComponentImpl> parser,
            GenomeProcessor processor) {
        this(config.getEnaXmlUrl(), parser, processor);
    }

    public EnaGenomeMaterializer(String string, EnaParser<GenomicComponentImpl> parser, GenomeProcessor processor) {
        this.parser = parser;
        this.enaXmlLoc = string;
        this.processor = processor;
    }

    public Genome materializeData(GenomeMetaData genomeMetaData) {
        Genome g = getGenome(genomeMetaData);
        processGenome(g);
        return g;
    }

    /**
     * @param g
     */
    protected void processGenome(Genome g) {
        if (processor != null) {
            getLog().info("Processing genome " + g.getName());
            processor.processGenome(g);
        }
    }

    /**
     * @param genomeMetaData
     * @return
     * @throws MaterializationException
     */
    protected Genome getGenome(GenomeMetaData genomeMetaData) {
        // 1. create a Genome stub
        Genome g = new GenomeImpl(genomeMetaData);
        // 2. get components for this genome
        if (genomeMetaData.getComponentMetaData().size() == 0) {
            throw new MaterializationUncheckedException("No components found for genome " + genomeMetaData.getName()
                    + " (ID: " + genomeMetaData.getId() + ")");
        }
        for (GenomicComponentMetaData md : genomeMetaData.getComponentMetaData()) {
            // 3. for each component, retrieve the XML file
            getLog().info("Parsing XML for accession " + md.getAccession() + " for genome " + g.getName() + " (id "
                    + g.getId() + ")");
            int retry_count = 0;
            boolean success = false;
            while (!success) {
                try {
                    GenomicComponentImpl c = getComponent(md.getAccession());
                    success = true;
                    addComponent(g, md, c);
                } catch (Throwable e) {
                    throw e;
                    // if (retry_count++ > 3) {
                    // throw new EnaParsingException(e.getMessage(), e, md);
                    // } else {
                    // getLog().warn("Parsing entry " + md.getAccession() + "
                    // failed, retrying...");
                    // try {
                    // Thread.sleep(10000);
                    // } catch (InterruptedException e1) {
                    // // swallow interruption as we don't care
                    // }
                    // }
                }
            }
        }
        genomeMetaData.setCreationDate(g.getCreationDate());
        genomeMetaData.setUpdateDate(g.getUpdateDate());
        return g;
    }

    public GenomicComponentImpl getComponent(String accession) {
        return parser.parse(getUrl(accession));
    }

    /**
     * Merge the parsed component with the genome object and metadata
     * 
     * @param g
     *            genome (including info)
     * @param md
     *            original metadata
     * @param c
     *            component and metadata as parsed
     */
    protected void addComponent(Genome g, GenomicComponentMetaData md, GenomicComponentImpl c) {
        md.setCreationDate(c.getMetaData().getCreationDate());
        md.setUpdateDate(c.getMetaData().getUpdateDate());
        md.setDescription(c.getMetaData().getDescription());
        // compare translation tables
        if (md.getGeneticCode() == GenomicComponentMetaData.NULL_GENETIC_CODE) {
            // no parsed version found
            if (c.getMetaData().getGeneticCode() == GenomicComponentMetaData.NULL_GENETIC_CODE) {
                // no information
                setDefaultGeneticCode(g, md);
            } else {
                // use the annotated version
                md.setGeneticCode(c.getMetaData().getGeneticCode());
            }
        }
        if (g.getCreationDate() == null || md.getCreationDate().before(g.getCreationDate())) {
            g.setCreationDate(md.getCreationDate());
        }
        if (g.getUpdateDate() == null || md.getUpdateDate().after(g.getUpdateDate())) {
            g.setUpdateDate(md.getUpdateDate());
        }
        c.setMetaData(md);
        c.setId(md.getIdentifier());
        c.setGenome(g);
        md.setComponentType(null);
        md.parseComponentDescription();
        g.addGenomicComponent(c);
    }

    public static final int DEFAULT_CODE = 1;
    public static final int BACTERIA_CODE = 11;

    protected void setDefaultGeneticCode(Genome g, GenomicComponentMetaData md) {
        // attempt to find a sensible default
        int code = DEFAULT_CODE;
        if (GenomeMetaData.BAC_SUPERREGNUM.equalsIgnoreCase(g.getSuperregnum())
                || GenomeMetaData.EUBAC_SUPERREGNUM.equalsIgnoreCase(g.getSuperregnum())
                || GenomeMetaData.ARC_SUPERREGNUM.equalsIgnoreCase(g.getSuperregnum())) {
            code = BACTERIA_CODE;
        }
        getLog().warn("No genetic code found for component " + md.getAccession() + " from genome " + g.getName()
                + "(id " + g.getId() + ") - using default value " + code);
        md.setGeneticCode(code);
    }

    protected URL getUrl(String accession) {
        String url = TemplateBuilder.template(this.enaXmlLoc, "ac", accession);
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new MaterializationUncheckedException("Could not parse URL " + url, e);
        }
    }

    public Collection<Genome> materializeData(Collection<GenomeMetaData> genomeMetaData) {
        Collection<Genome> gs = CollectionUtils.createArrayList(genomeMetaData.size());
        for (GenomeMetaData g : genomeMetaData) {
            gs.add(materializeData(g));
        }
        return gs;
    }

}
