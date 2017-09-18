package org.ensembl.genomeloader.genomebuilder.model;

/**
 * Interface representing a transcript associated with an RNA gene
 * @author dstaines
 *
 */
public interface RnaTranscript extends Integr8ModelComponent, Locatable,
		CrossReferenced, Identifiable {
	/**
	 * Is pseudogene ?
	 *
	 * @return is pseudogene ?
	 */
	boolean isPseudogene();

	/**
	 * The name of a gene is the agreed name used for convenience in identifying
	 * it
	 *
	 * @return agreed gene name
	 */
	String getName();

	/**
	 * The description of a gene.
	 *
	 * @return gene description
	 */
	String getDescription();

	void setDescription(String description);

	/**
	 * The biotype (an Ensembl concept) of an Rnagene
	 */
	String getBiotype();

	/**
	 * Returns the type of analysis applied to the Rnagene
	 */
	String getAnalysis();
	
	Rnagene getGene();
}
