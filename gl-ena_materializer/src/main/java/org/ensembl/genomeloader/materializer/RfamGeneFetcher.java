package org.ensembl.genomeloader.materializer;

import java.util.Collection;

import org.ensembl.genomeloader.genomebuilder.model.Rnagene;

/**
 * Interface for classes returning RFAM Rnagenes for a given INSDC accession
 * 
 * @author dstaines
 *
 */

public interface RfamGeneFetcher {

	public Collection<Rnagene> fetchGenes(String accession);

}
