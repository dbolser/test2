package uk.ac.ebi.proteome.materializer.ena.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;

public class ComponentAccessionNamingProcessor implements GenomeProcessor {

	private final Log log = LogFactory.getLog(this.getClass());

	public void processGenome(Genome genome) {
		log.info("Setting component names to accessions for genome "
				+ genome.getId());
		for (final GenomicComponent component : genome.getGenomicComponents()) {
			if (!component.getMetaData().getComponentType()
					.equals(GenomicComponentType.CONTIG)) {
				component.getMetaData().setName(
						component.getMetaData().getAccession());
				component.getMetaData().setComponentType(
						GenomicComponentType.SUPERCONTIG);
			}
		}
		log.info("Completed setting component names to accessions for genome "
				+ genome.getId());
	}

}
