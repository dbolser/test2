package org.ensembl.genomeloader.materializer.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.genomebuilder.metadata.GenomicComponentMetaData.GenomicComponentType;
import org.ensembl.genomeloader.genomebuilder.model.Genome;
import org.ensembl.genomeloader.genomebuilder.model.GenomicComponent;

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
