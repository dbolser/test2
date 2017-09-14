package uk.ac.ebi.proteome.materializer.ena.processors;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.ena.EnaGenomeConfig;
import uk.ac.ebi.proteome.materializer.ena.RfamGeneFetcher;
import uk.ac.ebi.proteome.materializer.ena.impl.RfamGeneFetcherImpl;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

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

	public RfamProcessor(SqlServiceTemplate rfamSrv,
			DatabaseReferenceType rfamType) {
		this(new RfamGeneFetcherImpl(rfamSrv, rfamType));
	}

	public RfamProcessor(EnaGenomeConfig config, ServiceContext context,
			DatabaseReferenceTypeRegistry registry) {
		this(new SqlServiceTemplateImpl(config.getRfamUri()), registry
				.getTypeForQualifiedName("Rfam", "ncrna"));
	}

	public void processGenome(Genome genome) {
		getLog().info("Fetching RFAM genes for " + genome.getName());
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			getLog().info("Fetching RFAM genes for " + component.getAccession());
			final Collection<Rnagene> genes = fetcher.fetchGenes(component
					.getVersionedAccession());
			getLog().info(
					"Fetched " + genes.size() + " RFAM genes for "
							+ component.getAccession());
			component.getRnagenes().addAll(genes);
		}
		getLog().info(
				"Fetching RFAM genes for " + genome.getName() + " completed");
	}

}
