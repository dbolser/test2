package uk.ac.ebi.proteome.materializer.ena;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import uk.ac.ebi.proteome.genomebuilder.materializer.EntityLocationSerializer;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.materializer.ena.genome_collections.GenomeCollections;
import uk.ac.ebi.proteome.materializer.ena.genome_collections.OracleGenomeCollections;

public class DumpEnaGenome {

    public static final void main(String[] args) throws Exception {
        String setChain = args[0];
        Log log = LogFactory.getLog(DumpEnaGenome.class);
        log.info("Dumping genome for "+setChain);
        EnaGenomeConfig config = EnaGenomeConfig.getConfig();
        GenomeCollections gc = new OracleGenomeCollections(config);
        GenomeMetaData genomeMetaData = gc.getGenomeForSetChain(setChain);
        log.info("Retrieved metadata for "+setChain);
        EnaGenomeMaterializer matfer = new EnaGenomeMaterializer(config);
        log.info("Dumping data for "+setChain);
        Genome genome = matfer.getGenome(genomeMetaData);
        log.info("Writing json for "+setChain);
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule simpleModule = new SimpleModule("SimpleModule", 
                                                  new Version(1,0,0,null,null,null));
        simpleModule.addSerializer(EntityLocation.class, new EntityLocationSerializer());
        mapper.registerModule(simpleModule);
        mapper.writeValue(new File(setChain + ".json"), genome);
        log.info("Completed writing json for "+setChain);
    }

}
