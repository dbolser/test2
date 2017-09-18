package org.ensembl.genomeloader.materializer;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.genomebuilder.metadata.GenomeMetaData;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReference;
import org.ensembl.genomeloader.genomebuilder.model.EntityLocation;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.materializer.executor.FileLockExecutor;
import org.ensembl.genomeloader.materializer.genome_collections.GenomeCollections;
import org.ensembl.genomeloader.materializer.genome_collections.OracleGenomeCollections;
import org.ensembl.genomeloader.materializer.processors.EnaGenomeProcessor;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DumpEnaGenome {

    public static final void main(String[] args) throws Exception {
        String setChain = args[0];
        Log log = LogFactory.getLog(DumpEnaGenome.class);
        log.info("Dumping genome for " + setChain);
        EnaGenomeConfig config = EnaGenomeConfig.getConfig();
        SqlService srv = new LocalSqlService();
        GenomeCollections gc = new OracleGenomeCollections(config, srv);
        GenomeMetaData genomeMetaData = gc.getGenomeForSetChain(setChain);
        log.info("Retrieved metadata for " + setChain);
        EnaGenomeMaterializer matfer = new EnaGenomeMaterializer(config, new EnaGenomeProcessor(config, srv),
                new FileLockExecutor(System.getProperty("user.dir"), 10));
        log.info("Dumping data for " + setChain);
        Genome genome = matfer.getGenome(genomeMetaData);
        log.info("Processing genome for " + setChain);
        matfer.processGenome(genome);
        log.info("Writing json for " + setChain);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null, null, null));
        simpleModule.addSerializer(EntityLocation.class, new EntityLocationSerializer());
        simpleModule.addSerializer(DatabaseReference.class, new DatabaseReferenceSerializer());
        simpleModule.addSerializer(Date.class, new DateSerializer());

        mapper.registerModule(simpleModule);
        mapper.writeValue(new File(setChain + ".json"), genome);
        log.info("Completed writing json for " + setChain);
    }

}
