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

package uk.ac.ebi.proteome.materializer.model.integrity.working;

import static uk.ac.ebi.proteome.materializer.model.ModelMaterializer.Policies.TIMING;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.Pseudogene;
import uk.ac.ebi.proteome.materializer.model.ModelDataHolder;
import uk.ac.ebi.proteome.materializer.model.ModelFinder;
import uk.ac.ebi.proteome.materializer.model.ModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.component.GenomicComponentModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.integrity.Base;
import uk.ac.ebi.proteome.mirror.sequence.SequenceInformation;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ModelFinding extends Base {

	private ModelDataHolder data;

	public void populateData(String component) {
		//data = new ModelMaterializer(getRegistry(), EnumSet.of(JAVA_MERGING_XREFS, TIMING)).getData(component);
		data = new ModelMaterializer(getRegistry(), EnumSet.of(TIMING)).getData(Long.valueOf(component));
	}

	@Test public void fullModel() {
//		String component = "U00096";
		String component = "AM910983";
//		String component = "AB011548";
		
		populateData(component);
		ModelFinder finder = new ModelFinder(data);

		Persistable<GenomicComponent> componentPersistable =
			CollectionUtils.getFirstElement(new GenomicComponentModelMaterializer(
				getRegistry()).getMaterializedDataInstance(
				component).getData(),null);

		GenomicComponent finalComponent = finder.find(componentPersistable);
		
		String version = finalComponent.getSequence().getProperties().get(SequenceInformation.PROPERTY_VERSION);

		List<Pseudogene> pg = new ArrayList<Pseudogene>(finalComponent.getPseudogenes());
		
		System.out.println("VERSION: "+version);
		System.out.println("FINISHED!!!");
	}

}
