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

package uk.ac.ebi.proteome.materializer.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Operon;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.Transcript;
import uk.ac.ebi.proteome.materializer.model.component.GenomicComponentModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.component.GenomicComponentSourceModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneNameModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneNameXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneXrefExtModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneXrefMergingModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.gene.GeneXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.genome.GenomeModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.operon.InternedOperonModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.operon.OperonLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.operon.OperonModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.operon.OperonXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinLocationModModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinSubLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinXrefExtModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinXrefMergingModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.protein.ProteinXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.proteinfeature.ProteinFeatureLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.proteinfeature.ProteinFeatureModModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.proteinfeature.ProteinFeatureModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.proteinfeature.ProteinFeatureSubLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.pseudogene.PseudogeneLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.pseudogene.PseudogeneModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.pseudogene.PseudogeneNameModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.pseudogene.PseudogeneNameXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.pseudogene.PseudogeneXrefModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.InternedTranscriptModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.TranscriptLocationModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.TranscriptModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.TranscriptXrefExtModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.TranscriptXrefMergingModelMaterializer;
import uk.ac.ebi.proteome.materializer.model.transcript.TranscriptXrefModelMaterializer;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.materializer.DataMaterializer;
import uk.ac.ebi.proteome.persistence.materializer.MaterializedDataInstance;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.util.reflection.aop.MethodInvocationTimingInterceptor;

/**
 * The class where the buck stops. This attempts to take the tedium of needing
 * to run all the materializers when you want a fully populated component back
 * (for the protein coding and pseudogene model).
 * 
 * <p>
 * If you do not want to use this object then you are free to populate the
 * objects accodingly since most finders make no assumption about the location
 * of the {@link MaterializedDataInstance} (apart from {@link ModelFinder} which
 * uses @{ModelDataHolder}). However this class should offer most of the
 * flexibility required.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ModelMaterializer {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	/**
	 * Returns a materializer which when used in conjunction with
	 * {@link ModelFinder} will create a fully formed non-redundant model (
	 * redundancy here is the same as the model going into the database).
	 */
	public static ModelMaterializer getModelMaterializer(ModelRegistry registry) {
		return new ModelMaterializer(registry, EnumSet.of(Policies.INTERNING,
				Policies.JAVA_MERGING_XREFS));
	}

	/**
	 * Returns a materializer which will work well in conjunction with full
	 * dumps of the genomebuilder model (where duplication in records is not a
	 * concern)
	 */
	public static ModelMaterializer getFullDumpMaterializer(
			ModelRegistry registry) {
		return new ModelMaterializer(registry, EnumSet
				.of(Policies.JAVA_MERGING_XREFS));
	}

	/**
	 * Provides a set of actions the materializer will carry out if found. Any
	 * policy involving Xrefs will be considered to be one policy with the
	 * natural ordering of these polciies being the order (i.e. specifying ext
	 * xrefs will prevent the merging xrefs from running).
	 * 
	 * <p>
	 * Core xrefs can only be run if the other Xref policies are not present
	 * (this is the default behaviour for all special actions).
	 */
	public static enum Policies {
		EXT_XREFS, JAVA_MERGING_XREFS, INTERNING, TIMING;
	}

	private EnumSet<Policies> policies;
	private Registry registry;

	/**
	 * Default constructor which initalises to defaults
	 */
	public ModelMaterializer(ModelRegistry registry) {
		this.registry = registry;
		this.policies = EnumSet.noneOf(Policies.class);
	}

	/**
	 * Constructor which lets you setup the policies you wish to use at runtime
	 */
	public ModelMaterializer(ModelRegistry registry, EnumSet<Policies> policies) {
		this.registry = registry;
		this.policies = policies;
	}

	public ModelDataHolder getData(Long genomeId) {
		ModelDataHolder holder = new ModelDataHolder();

		getLog().debug("Materializing genome for " + genomeId);
		holder.genome = run(new GenomeModelMaterializer(registry), genomeId);
		// Running population
		holder.components = run(
				new GenomicComponentModelMaterializer(registry), genomeId);
		holder.componentToGene = run(new GeneModelMaterializer(registry),
				genomeId);
		holder.componentToDataItem = run(
				new GenomicComponentSourceModelMaterializer(registry), genomeId);

		holder.geneToLocation = run(
				new GeneLocationModelMaterializer(registry), genomeId);
		holder.geneToXref = runGeneXref(genomeId);
		holder.geneToName = run(new GeneNameModelMaterializer(registry),
				genomeId);
		holder.geneNameToXref = run(
				new GeneNameXrefModelMaterializer(registry), genomeId);

		holder.componentToPseudogene = run(new PseudogeneModelMaterializer(
				registry), genomeId);
		holder.pseudogeneToLocation = run(
				new PseudogeneLocationModelMaterializer(registry), genomeId);
		holder.pseudogeneToXref = run(new PseudogeneXrefModelMaterializer(
				registry), genomeId);
		holder.pseudogeneToName = run(new PseudogeneNameModelMaterializer(
				registry), genomeId);
		holder.pseudogeneNameToXref = run(
				new PseudogeneNameXrefModelMaterializer(registry), genomeId);

		holder.geneToProtein = run(new ProteinModelMaterializer(registry),
				genomeId);
		holder.proteinToLocation = run(new ProteinLocationModelMaterializer(
				registry), genomeId);
		holder.proteinLocationToSubLocation = run(
				new ProteinSubLocationModelMaterializer(registry), genomeId);
		holder.proteinLocationToProteinLocationMod = run(
				new ProteinLocationModModelMaterializer(registry), genomeId);
		holder.proteinToXref = runProteinXref(genomeId);

		holder.proteinToProteinFeature = run(
				new ProteinFeatureModelMaterializer(registry), genomeId);
		holder.proteinFeatureToLocation = run(
				new ProteinFeatureLocationModelMaterializer(registry), genomeId);
		holder.proteinFeatureLocationToSubLocation = run(
				new ProteinFeatureSubLocationModelMaterializer(registry),
				genomeId);
		holder.proteinFeatureLocationToProteinLocationMod = run(
				new ProteinFeatureModModelMaterializer(registry), genomeId);

		holder.proteinToTranscript = runTranscript(genomeId);
		holder.transcriptToLocation = run(
				new TranscriptLocationModelMaterializer(registry), genomeId);
		holder.transcriptToXref = runTranscriptXref(genomeId);

		holder.transcriptToOperon = runOperon(genomeId);
		holder.operonToLocation = run(new OperonLocationModelMaterializer(
				registry), genomeId);
		holder.operonToXref = run(new OperonXrefModelMaterializer(registry),
				genomeId);

		return holder;
	}

	protected <T, Q> MaterializedDataInstance<T, Q> run(
			DataMaterializer<T, Q> materializer, Object param) {
		getLog().debug(
				"Running" + materializer.getClass().getSimpleName()
						+ " with param " + param);
		return wrap(materializer).getMaterializedDataInstance(param);
	}

	@SuppressWarnings("unchecked")
	protected <T, Q> DataMaterializer<T, Q> wrap(
			DataMaterializer<T, Q> materializer) {
		DataMaterializer<T, Q> output = materializer;
		if (policies.contains(Policies.TIMING)) {
			output = (DataMaterializer<T, Q>) MethodInvocationTimingInterceptor
					.generateProxy(DataMaterializer.class, materializer);
		}
		return output;
	}

	/**
	 * DEFAULTS TO USING BASE GENE XREFS
	 */
	protected MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Gene> runGeneXref(
			Object param) {
		if (policies.contains(Policies.EXT_XREFS)) {
			return run(new GeneXrefExtModelMaterializer(registry), param);
		} else if (policies.contains(Policies.JAVA_MERGING_XREFS)) {
			return run(new GeneXrefMergingModelMaterializer(registry), param);
		} else {
			return run(new GeneXrefModelMaterializer(registry), param);
		}
	}

	/**
	 * DEFAULTS TO USING BASE PROTEIN XREFS
	 */
	protected MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Protein> runProteinXref(
			Object param) {
		if (policies.contains(Policies.EXT_XREFS)) {
			return run(new ProteinXrefExtModelMaterializer(registry), param);
		} else if (policies.contains(Policies.JAVA_MERGING_XREFS)) {
			return run(new ProteinXrefMergingModelMaterializer(registry), param);
		} else {
			return run(new ProteinXrefModelMaterializer(registry), param);
		}
	}

	/**
	 * DEFAULTS TO USING BASE TRANSCRIPT XREFS
	 */
	protected MaterializedDataInstance<Collection<Persistable<DatabaseReference>>, Transcript> runTranscriptXref(
			Object param) {
		if (policies.contains(Policies.EXT_XREFS)) {
			return run(new TranscriptXrefExtModelMaterializer(registry), param);
		} else if (policies.contains(Policies.JAVA_MERGING_XREFS)) {
			return run(new TranscriptXrefMergingModelMaterializer(registry),
					param);
		} else {
			return run(new TranscriptXrefModelMaterializer(registry), param);
		}
	}

	/**
	 * DEFAULTS TO USING NO MODEL INTERNING
	 */
	protected MaterializedDataInstance<Collection<Persistable<Transcript>>, Protein> runTranscript(
			Object param) {
		if (policies.contains(Policies.INTERNING)) {
			return run(new InternedTranscriptModelMaterializer(registry), param);
		} else {
			return run(new TranscriptModelMaterializer(registry), param);
		}
	}

	/**
	 * DEFAULTS TO USING NO MODEL INTERNING
	 */
	protected MaterializedDataInstance<Collection<Persistable<Operon>>, Transcript> runOperon(
			Object param) {
		if (policies.contains(Policies.INTERNING)) {
			return run(new InternedOperonModelMaterializer(registry), param);
		} else {
			return run(new OperonModelMaterializer(registry), param);
		}
	}

	public void addPolicies(Policies... policies) {
		addPolicies(EnumSet.copyOf(Arrays.asList(policies)));
	}

	public void addPolicies(EnumSet<Policies> policies) {
		policies.addAll(policies);
	}

	public void clearPolicies() {
		policies = EnumSet.noneOf(Policies.class);
	}
}
