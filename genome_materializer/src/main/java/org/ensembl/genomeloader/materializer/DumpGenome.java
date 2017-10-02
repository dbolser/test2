package org.ensembl.genomeloader.materializer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.executor.FileLockExecutor;
import org.ensembl.genomeloader.materializer.genome_collections.GenomeCollections;
import org.ensembl.genomeloader.materializer.genome_collections.OracleGenomeCollections;
import org.ensembl.genomeloader.materializer.impl.MaterializationUncheckedException;
import org.ensembl.genomeloader.materializer.processors.EnaGenomeProcessor;
import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.model.DatabaseReference;
import org.ensembl.genomeloader.model.EntityLocation;
import org.ensembl.genomeloader.model.GeneName;
import org.ensembl.genomeloader.model.Genome;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.services.sql.impl.LocalSqlService;
import org.ensembl.genomeloader.validator.EnaGenomeValidator;
import org.ensembl.genomeloader.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class DumpGenome {

    @SuppressWarnings("static-access")
    public static final void main(String[] args) throws Exception {

        Options options = new Options();
        options.addOption(OptionBuilder.withArgName("accession").withLongOpt("set_chain")
                .withDescription("INSDC set chain (unversioned)").hasArg().isRequired().create("s"));
        options.addOption(OptionBuilder.withArgName("file").withLongOpt("dump_file").withDescription("JSON output file")
                .hasArg().isRequired(false).create("f"));
        options.addOption(OptionBuilder.withArgName("config_file").withLongOpt("config_file")
                .withDescription("XML config file").hasArg().isRequired(false).create("c"));
        Log log = LogFactory.getLog(DumpGenome.class);
        CommandLineParser parser = new BasicParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String setChain = cmd.getOptionValue('s');
            log.debug("Using setChain " + setChain);
            String file;
            if (cmd.hasOption('f')) {
                file = cmd.getOptionValue('f');
                log.debug("Using specified output file " + file);
            } else {
                file = setChain + ".json";
                log.debug("Using default output file " + file);
            }
            EnaGenomeConfig config;
            if (cmd.hasOption('c')) {
                log.debug("Using specified config file " + cmd.getOptionValue('c'));
                config = EnaGenomeConfig.readConfig(cmd.getOptionValue('c'));
            } else {
                log.debug("Using default config file");
                config = EnaGenomeConfig.getConfig();
            }
            new DumpGenome(config, new LocalSqlService()).dumpGenome(setChain, file);
        } catch (MissingOptionException e) {
            System.err.println(e.getMessage());
            new HelpFormatter().printHelp(DumpGenome.class.getSimpleName(), options, true);
            System.exit(1);
        }
    }

    Log log = LogFactory.getLog(this.getClass());
    private final EnaGenomeConfig config;
    private final SqlService srv;

    public DumpGenome(EnaGenomeConfig config, SqlService srv) {
        this.config = config;
        this.srv = srv;
    }

    public void dumpGenome(String setChain, String file) {
        Genome genome = materializeGenome(setChain);
        dumpGenomeJson(genome, new File(file));
    }

    public Genome materializeGenome(String setChain) {
        log.info("Retrieving metadata for " + setChain);
        GenomeCollections gc = new OracleGenomeCollections(config, srv);
        GenomeMetaData genomeMetaData = gc.getGenomeForSetChain(setChain);
        if (genomeMetaData == null) {
            throw new MaterializationUncheckedException("Could not find assembly for " + setChain);
        }
        log.info("Retrieved metadata for " + setChain);
        return materializeGenome(genomeMetaData);
    }

    public Genome materializeGenome(GenomeMetaData genomeMetaData) {
        EnaGenomeMaterializer matfer = new EnaGenomeMaterializer(config.getEnaXmlUrl(),
                new EnaParser(new FileLockExecutor(config.getLockFileDir(), config.getMaxEnaConnections()),
                        new XmlDatabaseReferenceTypeRegistry()),
                new EnaGenomeProcessor(config, srv), new EnaGenomeValidator(config));
        log.info("Dumping data for " + genomeMetaData.getId());
        Genome genome = matfer.getGenome(genomeMetaData);
        log.info("Processing genome for " + genomeMetaData.getId());
        matfer.processGenome(genome);
        log.info("Validating genome for " + genomeMetaData.getId());
        matfer.validateGenome(genome);
        return genome;
    }

    public void dumpGenomeJson(Genome genome, File file) {
        log.info("Writing json for " + genome.getIdString() + " to " + file.getPath());
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        SimpleModule simpleModule = new SimpleModule("SimpleModule", new Version(1, 0, 0, null, null, null));
        simpleModule.addSerializer(EntityLocation.class, new EntityLocationSerializer());
        simpleModule.addSerializer(DatabaseReference.class, new DatabaseReferenceSerializer());
        simpleModule.addSerializer(Date.class, new DateSerializer());
        simpleModule.addSerializer(GeneName.class, new GeneNameSerializer());
        mapper.registerModule(simpleModule);
        try {
            mapper.writeValue(file, genome);
            log.info("Completed writing json for " + genome.getIdString());
        } catch (IOException e) {
            throw new GenomeDumpException("Could not write JSON for " + genome.getIdString() + " to " + file.getPath(),
                    e);
        }
    }

}
