/*
 * Copyright [2009-2014] EMBL-European Bioinformatics Institute
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * GenomeCollections
 * 
 * @author dstaines
 * @author $Author$
 * @version $Revision$
 */
package org.ensembl.genomeloader.materializer.genome_collections;

import java.util.List;

import org.ensembl.genomeloader.metadata.GenomeMetaData;
import org.ensembl.genomeloader.metadata.GenomicComponentMetaData;

/**
 * @author dstaines
 *
 */
public interface GenomeCollections {
	
	public static final String ENA_SRC = "ENA";

	public abstract GenomeMetaData getGenomeForSetChain(String setChain);

	public abstract GenomeMetaData getGenomeForOrganism(String name);

	public abstract GenomeMetaData getGenomeForTaxId(int taxId);

	public abstract GenomeMetaData getGenomeForEnaAccession(String accession);

	public abstract String getSetChainForOrganism(String name);

	public abstract String getSetChainForTaxId(int taxId);

	public abstract String getSetChainForEnaAccession(String accession);

	public abstract List<GenomicComponentMetaData> getComponentsForGenome(GenomeMetaData md);

	public abstract List<GenomicComponentMetaData> getComponentsForGenome(String setChain, String version);

}
