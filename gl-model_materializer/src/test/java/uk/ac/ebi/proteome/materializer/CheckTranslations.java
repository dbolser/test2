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
 * File: CheckTranslations.java
 * Created by: dstaines
 * Created on: Dec 3, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.materializer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.materializer.GenomeMaterializer;
import uk.ac.ebi.proteome.genomebuilder.model.Gene;
import uk.ac.ebi.proteome.genomebuilder.model.Genome;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.ModelUtils;
import uk.ac.ebi.proteome.genomebuilder.model.Protein;
import uk.ac.ebi.proteome.genomebuilder.model.TranslationStatus;
import uk.ac.ebi.proteome.materializer.model.DbBackedGenomeMaterializer;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

/**
 * @author dstaines
 * 
 */
public class CheckTranslations {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Log log = LogFactory.getLog(DbBackedGenomeMaterializer.class);
		String uri = "jdbc:oracle:thin:integr8/integr8_prot@localhost:15310:PRPRO";
		ServiceContext context = ServiceContext.getInstance();
		GenomeMaterializer dumper = new DbBackedGenomeMaterializer(context, uri);
		String sql = "select p.proteome_id from proteomes.proteome p where p.ensembl_genomes_id>0";
		for (String ac : new SqlServiceTemplateImpl(uri, context)
				.queryForDefaultObjectList(sql, String.class)) {
			// for(String ac : new String[]{
			// "AL009126",
			// "AL123456"}) {
			try {
				log.info("Materializing " + ac);
				Genome genome = dumper.getGenome(ac);
				log.info("Checking " + ac);
				for (GenomicComponent component : genome.getGenomicComponents()) {
					for (Gene gene : component.getGenes()) {
						for (Protein protein : gene.getProteins()) {
							TranslationStatus status = ModelUtils
									.checkTranslation(component, protein);
							if (status != TranslationStatus.TRANSLATABLE) {
								// !LocationUtils.isLocationFuzzy(protein.getLocation())
								// &&
								log.warn("Protein " + protein.getIdString()
										+ " at " + protein.getLocation()
										+ " does not translate: " + status);
							}
						}
					}
				}
			} catch (Throwable e) {
				log.debug(e);
				log.warn("Could not process " + ac + ": " + e.getMessage());
			}
		}
	}
}
