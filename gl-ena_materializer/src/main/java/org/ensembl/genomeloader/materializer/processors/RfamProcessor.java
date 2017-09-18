package org.ensembl.genomeloader.materializer.processors;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.genomebuilder.model.DatabaseReferenceType;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;
import org.ensembl.genomeloader.genomebuilder.model.Rnagene;
import org.ensembl.genomeloader.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.RfamGeneFetcher;
import org.ensembl.genomeloader.materializer.impl.RfamGeneFetcherImpl;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;

/**
 * {@link GenomeProcessor} to add {@link Rnagene} instances from RFAM to a
 * {@link Genome}
 * 
 * @author dstaines
 *
 */
public class RfamProcessor implements GenomeProcessor {

    private Log log;

    private final RfamGeneFetcher fetcher;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public RfamProcessor(RfamGeneFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public RfamProcessor(SqlServiceTemplate rfamSrv, DatabaseReferenceType rfamType) {
        this(new RfamGeneFetcherImpl(rfamSrv, rfamType));
    }

    public RfamProcessor(EnaGenomeConfig config, SqlService srv, DatabaseReferenceTypeRegistry registry) {
        this(new SqlServiceTemplateImpl(config.getRfamUri(), srv), registry.getTypeForQualifiedName("Rfam", "ncrna"));
    }

    public void processGenome(Genome genome) {
        getLog().info("Fetching RFAM genes for " + genome.getName());
        for (final GenomicComponent component : genome.getGenomicComponents()) {
            getLog().info("Fetching RFAM genes for " + component.getAccession());
            final Collection<Rnagene> genes = fetcher.fetchGenes(component.getVersionedAccession());
            getLog().info("Fetched " + genes.size() + " RFAM genes for " + component.getAccession());
            component.getRnagenes().addAll(genes);
        }
        getLog().info("Fetching RFAM genes for " + genome.getName() + " completed");
    }

}
