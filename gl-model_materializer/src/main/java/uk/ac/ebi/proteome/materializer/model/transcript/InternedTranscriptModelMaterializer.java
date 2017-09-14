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

package uk.ac.ebi.proteome.materializer.model.transcript;

import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.materializer.misc.CollectionRedundancyFilterMaterializer;
import uk.ac.ebi.proteome.materializer.model.operon.InternedOperonModelMaterializer;
import uk.ac.ebi.proteome.registry.Registry;

/**
 * Same as {@link InternedOperonModelMaterializer} but uses an instance of
 * {@link TranscriptModelMaterializer} as the source data.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class InternedTranscriptModelMaterializer extends CollectionRedundancyFilterMaterializer<Transcript, Protein> {

	public InternedTranscriptModelMaterializer(Registry registry) {
		super(new TranscriptModelMaterializer(registry));
	}
}
