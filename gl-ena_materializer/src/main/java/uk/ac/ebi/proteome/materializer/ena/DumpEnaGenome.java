package uk.ac.ebi.proteome.materializer.ena;

import java.io.File;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.ac.ebi.proteome.genomebuilder.materializer.DatabaseReferenceSerializer;
import uk.ac.ebi.proteome.genomebuilder.materializer.DateSerializer;
import uk.ac.ebi.proteome.genomebuilder.materializer.EntityLocationSerializer;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.materializer.ena.executor.FileLockExecutor;
import uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections;
import uk.ac.ebi.proteome.materializer.ena.genome_collections.OracleGenomeCollections;
import uk.ac.ebi.proteome.materializer.ena.processors.EnaGenomeProcessor;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;

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
