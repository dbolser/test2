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

package uk.ac.ebi.proteome.materializer.ena;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.services.ServiceContext;

public class IdentifyGenome {

	public static final void main(String[] args) throws Exception {
		EnaGenomeConfig config = EnaGenomeConfig.getConfig();
		EnaGenomeIdentifier idfer = new EnaGenomeIdentifier(ServiceContext
				.getInstance(), config);
		GenomeMetaData gd = idfer.getMetaDataForIdentifier("32054");
		System.out.println(gd.getComponentMetaData().size());
		for(GenomicComponentMetaData md: gd.getComponentMetaData()) {
			System.out.println(md.getAccession()+" "+md.getDescription());
		}
	}
	
}
