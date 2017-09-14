package uk.ac.ebi.proteome.materializer.ena;

import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;

/**
 * Interface for classes returning RFAM Rnagenes for a given INSDC accession
 * 
 * @author dstaines
 *
 */

public interface RfamGeneFetcher {

	public Collection<Rnagene> fetchGenes(String accession);

}
