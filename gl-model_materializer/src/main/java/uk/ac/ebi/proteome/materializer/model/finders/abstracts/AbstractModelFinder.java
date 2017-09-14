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

package uk.ac.ebi.proteome.materializer.model.finders.abstracts;

import static uk.ac.ebi.proteome.util.collections.CollectionUtils.createArrayList;

import java.util.Collection;
import java.util.List;

import uk.ac.ebi.proteome.genomebuilder.model.AnnotatedGene;
import uk.ac.ebi.proteome.genomebuilder.model.CrossReferenced;
import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.GeneName;
import uk.ac.ebi.proteome.genomebuilder.model.Locatable;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.finder.Finder;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;

/**
 * Base object for processing of a model object. This allows us to selectivly
 * add new processing points to the post process model method and create
 * a more fully formed object.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class AbstractModelFinder <M, Q> implements Finder<Persistable<Q>,Collection<Persistable<M>>> {

	private final MaterializedDataInstance<Collection<Persistable<M>>, Q> mdi;
	private List<PostProcessor<M>> postProcessors = createArrayList();

	public AbstractModelFinder(MaterializedDataInstance<Collection<Persistable<M>>, Q> mdi) {
		this.mdi = mdi;
	}

	/**
	 * Returns this to allow for chaining of post processor addition
	 */
	protected AbstractModelFinder<M,Q> addPostProcessor(PostProcessor<M> processor) {
		this.postProcessors.add(processor);
		return this;
	}

	public Collection<Persistable<M>> find(Persistable<Q> query) {
		Collection<Persistable<M>> data = createArrayList();

		for(Persistable<M> persistableModel: mdi.getData(query)) {
			for(PostProcessor<M> processor: postProcessors) {
				persistableModel = processor.process(persistableModel);
			}
			data.add(persistableModel);
		}
		return data;
	}

	/**
	 * Allows for the addition of post procesing steps using generics. It is
	 * recommended that these are bound as late to the problem as possible to
	 * aid the generic binding of the implementations.
	 */
	protected static interface PostProcessor<M> {
		public Persistable<M> process(Persistable<M> model);
	}

	/**
	 * Uses an Xref finder to retrieve xrefs and adds them to the model object
	 * implementing {@link CrossReferenced}
	 *
	 * @author $Author$
	 * @author ayates
	 */
	protected static class XrefPostProcessor<M extends CrossReferenced> implements PostProcessor<M> {

		private final Finder<Persistable<M>, Collection<DatabaseReference>> xrefFinder;

		public XrefPostProcessor(Finder<Persistable<M>, Collection<DatabaseReference>> xrefFinder) {
			this.xrefFinder = xrefFinder;
		}

		public Persistable<M> process(Persistable<M> model) {
			for(DatabaseReference xref: xrefFinder.find(model)) {
				model.getPersistableObject().addDatabaseReference(xref);
			}
			return model;
		}
	}

	/**
	 * Uses an EntityLocation finder to retrieve locations and adds them to
	 * the model object implementing {@link Locatable}
	 *
	 * @author $Author$
	 * @author ayates
	 */
	protected static class LocationPostProcessor<M extends Locatable> implements PostProcessor<M> {

		private final Finder<Persistable<M>, Persistable<EntityLocation>> locationFinder;

		public LocationPostProcessor(Finder<Persistable<M>, Persistable<EntityLocation>> locationFinder) {
			this.locationFinder = locationFinder;
		}

		public Persistable<M> process(Persistable<M> model) {
			Persistable<EntityLocation> location = locationFinder.find(model);
			if(location != null) {
				model.getPersistableObject().setLocation(location.getPersistableObject());
			}
			return model;
		}
	}

	/**
	 * Takes a GeneName finder and adds those names to the current
	 * {@link AnnotatedGene} object.
	 *
	 * @author $Author$
	 * @author ayates
	 */
	protected static class GeneNamePostProcessor<M extends AnnotatedGene> implements PostProcessor<M> {

		private final Finder<Persistable<M>, Collection<GeneName>> geneNameFinder;

		public GeneNamePostProcessor(Finder<Persistable<M>, Collection<GeneName>> geneNameFinder) {
			this.geneNameFinder = geneNameFinder;
		}

		public Persistable<M> process(Persistable<M> model) {
			M underlyingModel = model.getPersistableObject();
			for(GeneName name: geneNameFinder.find(model)) {
				underlyingModel.addGeneName(name);
			}
			return model;
		}
	}
}
