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

package org.ensembl.genomeloader.materializer.ena;

import java.util.Collection;

import org.ensembl.genomeloader.genomebuilder.metadata.GenomeMetaData;
import org.ensembl.genomeloader.materializer.AcOnlyEnaGenomeMaterializer;
import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.EnaGenomeMaterializer;
import org.ensembl.genomeloader.materializer.identifiers.EnaComponentSetIdentifier;
import org.ensembl.genomeloader.util.collections.CollectionUtils;

public class MaterializeAccession {

	public static final void main(String[] args) throws Exception {
		EnaGenomeConfig config = EnaGenomeConfig.getConfig();
        EnaComponentSetIdentifier idfer = new EnaComponentSetIdentifier(
				config);
		Collection<String> componentAcs = CollectionUtils.createArrayList(
				args);
		GenomeMetaData gd = idfer.getMetaDataForIdentifier("12345",
				componentAcs);
		EnaGenomeMaterializer mat = new AcOnlyEnaGenomeMaterializer(
				EnaGenomeConfig.getConfig());
		mat.materializeData(gd);
	}

}
