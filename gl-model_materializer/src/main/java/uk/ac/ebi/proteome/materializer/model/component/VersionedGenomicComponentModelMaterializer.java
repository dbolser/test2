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

package uk.ac.ebi.proteome.materializer.model.component;

import java.util.Collection;
import java.util.Date;
import java.util.Map.Entry;

import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.materializer.version.VersionUtils;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.resolver.DataItem;
import uk.ac.ebi.proteome.services.version.VersionService;

/**
 * Runs the early finder code for bringing back a fuller populated model for use
 * in later parts of the materialization process. Whilst this is not the
 * intended divide between finders and materializers it serves a purpose.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class VersionedGenomicComponentModelMaterializer extends
		GenomicComponentModelMaterializer {

	private final DataMaterializer<Collection<DataItem>, GenomicComponent> genomicComponentSourceMaterializer;
	private final VersionService srv;

	public VersionedGenomicComponentModelMaterializer(Registry registry,
			VersionService srv) {
		super(registry);
		this.genomicComponentSourceMaterializer = new GenomicComponentSourceModelMaterializer(
				registry);
		this.srv = srv;
	}

	public MaterializedDataInstance<Collection<Persistable<GenomicComponent>>, Genome> getMaterializedDataInstance(
			Object... args) {
		MaterializedDataInstance<Collection<Persistable<GenomicComponent>>, Genome> mdi = super
				.getMaterializedDataInstance(args);
		MaterializedDataInstance<Collection<DataItem>, GenomicComponent> dataItemMdi = genomicComponentSourceMaterializer
				.getMaterializedDataInstance(args);
		for (Entry<Object, Collection<Persistable<GenomicComponent>>> e : mdi
				.getMap().entrySet()) {
			for (Persistable<GenomicComponent> c : e.getValue()) {
				GenomicComponent gc = c.getPersistableObject();
				// decorate with some version info
				VersionUtils.versionComponent(srv, gc);
				Date dateForComponent = VersionUtils.getDateForComponent(srv, gc);
				gc.getMetaData().setCreationDate(
						dateForComponent);
				gc.getMetaData().setUpdateDate(
						dateForComponent);
				gc.getMetaData()
						.setVersion(
								String.valueOf(VersionUtils
										.getVersionForComponent(gc)));
				for (DataItem dataItem : dataItemMdi.getData(c)) {
					gc.getMetaData().addDataItem(dataItem);
				}
			}
		}
		return mdi;
	}
}
