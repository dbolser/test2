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
 * File: NcrnageneModelMaterializer.java
 * Created by: dstaines
 * Created on: Nov 4, 2009
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer.model.rnagene;

import java.util.ArrayList;
import java.util.Collection;

import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.registry.Registry;

/**
 * @author dstaines
 *
 */
public class RrnageneModelMaterializer extends MergingRnageneModelMaterializer {

	public RrnageneModelMaterializer(final Registry registry) {
		super(
				new ArrayList<DataMaterializer<Collection<Persistable<Rnagene>>, GenomicComponent>>() {
					{
						add(new EmblRrnageneModelMaterializer(registry));
						add(new PredictedRrnageneModelMaterializer(registry));
					}
				});
	}
}
