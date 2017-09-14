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

package uk.ac.ebi.proteome.materializer.model.mappers;

import java.sql.SQLException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.GeneNameType;
import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GeneNameImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.RnageneImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.biojava.LocationUtils;

/**
 * @author $Author$
 * @version $Revision$
 */
public class EmblRnageneModelMapRowMapper extends
		AbstractCollectionMapRowMapper<Persistable<Rnagene>> {

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	// Various Patterns to parse the note qualifier for tRNAs

	private final static Pattern descriptionPattern = Pattern
			.compile("tRNA ([^\\s]+) anticodon (\\w+).*");

	private final static Pattern descriptionPattern2 = Pattern
			.compile("anticodon (\\w+).*");

	private final static Pattern descriptionPattern3 = Pattern
			.compile("anticodon=(\\w+), (\\w+)");

	/**
	 *
	 */
	private static final String EMBL_NCRNA = "ncRNA-EMBL";

	public void existingObject(Collection<Persistable<Rnagene>> currentValue,
			ROResultSet resultSet, int position) throws SQLException {
		currentValue.add(new SimpleWrapperPersistable<Rnagene>(
				makeRnagene(resultSet), StringUtils.EMPTY));
	}

	protected Rnagene makeRnagene(ROResultSet resultSet) throws SQLException {

		/*
		 * select
		 * location,genenames,locustags,product_name,note,gene_type,IS_PSEUDO
		 * from embl_ncrna
		 */

		EntityLocation location = new DelegatingEntityLocation(LocationUtils
				.parseEmblLocation(resultSet.getString(2)));
		RnageneImpl rnagene = new RnageneImpl();
		rnagene.setAnalysis(EMBL_NCRNA);

		String geneName = resultSet.getString(3);
		String locusTag = resultSet.getString(4);

		String productName = resultSet.getString(5);
		String note = resultSet.getString(6);

		String biotype = resultSet.getString(7);
		boolean isPseudo = "1".equalsIgnoreCase(resultSet.getString(8));

		// Todo: One case where product name is empty (tRNA pseudo)
		// then productName = "tRNA Pseudo"
		// if note.toLowerCase matches trna pseudo

		// Todo: Similar situation for MT pombe tRNAs
		// get the product name from parsing the note
		// anticodon=cau, met

		if ((productName == null) && (biotype.equals("tRNA"))) {
			if (note.toLowerCase().contains("trna pseudo")) {
				productName = "tRNA Pseudo";
			} else {
				Matcher descriptionMatcher3 = descriptionPattern3.matcher(note);
				if (descriptionMatcher3.matches()) {
					String aminoAcid = descriptionMatcher3.group(2);
					// Capitalize the first letter
					aminoAcid = aminoAcid.substring(0, 1).toUpperCase()
							+ aminoAcid.substring(1).toLowerCase();
					productName = "tRNA " + aminoAcid;
				} else {
					// Todo: add a report
				}

			}
		} else if ((biotype.equals("tRNA")) && (productName != null)
				&& isPseudo) {
			if (!productName.toLowerCase().contains("pseudo")) {
				// productName should be 'tRNA pseudo'
				getLog().debug(
						"tRNA pseudogene, but productName is not consistent, "
								+ productName + ": replace it by tRNA pseudo");
				productName = "tRNA pseudo";
			}
		}

		// Ensembl gene name
		// Use the /gene and then locus_tag as a synonym
		// if no /gene, use locus_tag as a gene name, and no synonym then

		String name = null;
		if (!StringUtils.isEmpty(geneName)) {
			rnagene.addGeneName(new GeneNameImpl(geneName, GeneNameType.NAME));
			name = geneName;
		}
		if (!StringUtils.isEmpty(locusTag)) {
			rnagene.addGeneName(new GeneNameImpl(locusTag,
					GeneNameType.ORDEREDLOCUSNAMES));
			if (StringUtils.isEmpty(name)) {
				name = locusTag;
			}
		}
		if (StringUtils.isEmpty(name)) {
			name = productName;
		}

		rnagene.setName(name);

		// The description is from the product name
		// except for tRNA genes, where it is from the note

		String description;
		if (biotype.equals("tRNA") && !StringUtils.isEmpty(note)) {

			description = note;

			Matcher descriptionMatcher = descriptionPattern
					.matcher(description);
			Matcher descriptionMatcher2 = descriptionPattern2
					.matcher(description);
			Matcher descriptionMatcher3 = descriptionPattern3
					.matcher(description);

			// tRNA Glu anticodon CTC, Cove score 74.15
			if (descriptionMatcher.matches()) {
				String aminoAcid = descriptionMatcher.group(1);
				String anticodon = descriptionMatcher.group(2)
						.replace("T", "U");

				description = "tRNA " + aminoAcid + " for anticodon "
						+ anticodon;
			}
			// anticodon CTC, Cove score 74.15
			else if (descriptionMatcher2.matches()) {
				String anticodon = descriptionMatcher2.group(1).replace("T",
						"U");
				description = productName + " for anticodon " + anticodon;
			}
			// anticodon=ucu, arg in MT EMBL entry
			else if (descriptionMatcher3.matches()) {
				String anticodon = descriptionMatcher3.group(1).toUpperCase();
				String aminoAcid = descriptionMatcher3.group(2);
				// Capitalize the first letter
				aminoAcid = aminoAcid.substring(0, 1).toUpperCase()
						+ aminoAcid.substring(1).toLowerCase();
				description = "tRNA " + aminoAcid + " for anticodon "
						+ anticodon;
			}

		} else {

			// for all other cases, ie tRNA with empty note or all other
			// biotypes
			// the description will come from the product name

			description = productName;

			if (productName == null) {
				getLog().debug(
						"no product name found for RNA, "
								+ resultSet.getString(7) + ", " + name);
				getLog().debug("gene startat: " + location.getMin());
				if (biotype.equals("misc_RNA")) {
					// one case in pombe, misc_RNA, so let's put the generic
					// name
					description = "non-coding RNA (predicted)";
					getLog().debug("Setting it to " + description);
				}
			}

			if (resultSet.getString(7).equals("rRNA")) {
				// some reformatting of the rRNAs
				description = description.replace(" rRNA", " ribosomal RNA");
			}
		}

		rnagene.setDescription(description);

		if (biotype.equals("tRNA") && isPseudo) {
			getLog().debug("tRNA gene, " + name + ", type set to pseudogene");
			biotype = "tRNA_pseudogene";
		}
		rnagene.setBiotype(biotype);

		rnagene.setPseudogene(isPseudo);
		rnagene.setLocation(location);
		return rnagene;
	}
}
