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
 * File: GenomicComponentMetaData.java
 * Created by: dstaines
 * Created on: Mar 9, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.genomebuilder.metadata;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.metadata.impl.DefaultGenomicComponentDescriptionHandler;
import uk.ac.ebi.proteome.genomebuilder.metadata.impl.GenomicComponentSpecificationImpl;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo;
import uk.ac.ebi.proteome.genomebuilder.model.sequence.Sequence;
import uk.ac.ebi.proteome.genomebuilder.model.sequence.SequenceMetaData;

/**
 * ResolverMetaData describing a genomic component, and optionally its
 * associated CDS meta data. Also contains some basic properties of the
 * component (scope, type, superregnum) to allow filtering.
 * 
 * @author dstaines
 * 
 */
public class GenomicComponentMetaData extends EntityMetaData implements
		GenomicComponentSpecification {

	/**
	 * Comparator for comparing two instances of
	 * {@link GenomicComponentMetaData} based on type and then rank
	 */
	public static final Comparator<GenomicComponentMetaData> COMPONENT_COMPARATOR = new Comparator<GenomicComponentMetaData>() {

		public int compare(GenomicComponentMetaData o1,
				GenomicComponentMetaData o2) {
			if (o1.getComponentType() != o2.getComponentType()) {
				// compare type rank
				return Integer
						.valueOf(o1.getComponentType().getRank())
						.compareTo(
								Integer.valueOf(o2.getComponentType().getRank()));
			} else {
				// compare length
				return Integer.valueOf(o1.getLength()).compareTo(
						Integer.valueOf(o2.getLength()));
			}
		}

	};

	public static enum GenomicComponentType {
		CHROMOSOME(1), PLASMID(2), SUPERCONTIG(3), CONTIG(4);
		GenomicComponentType(int rank) {
			this.rank = rank;
		}

		int rank;

		public int getRank() {
			return rank;
		}
	};

	public static final int NULL_GENETIC_CODE = -1;

	private static final long serialVersionUID = 1L;

	public GenomicComponentMetaData() {
		this.spec = new GenomicComponentSpecificationImpl();
	}

	public GenomicComponentMetaData(String src, String id, String accession,
			GenomeInfo genome) {
		super(src, id);
		this.spec = new GenomicComponentSpecificationImpl();
		this.spec.setAccession(accession);
		this.spec.setGenomeInfo(genome);
	}

	private GenomicComponentDescriptionHandler descriptionHandler = new DefaultGenomicComponentDescriptionHandler();
	private final GenomicComponentSpecification spec;
	private String version;
	private Date creationDate;
	private Date updateDate;
	private GenomicComponentType componentType;
	private String name;
	private String masterAccession;
	public static final String PROTEIN_SOURCE_TYPE = "protSrc";
	private Set<String> synonyms;
	private String versionedAccession;
	private boolean con;

	public static final String DNA_SOURCE_TYPE = "dnaSrc";

	private SequenceMetaData seqMd = null;

	public SequenceMetaData getSequenceMetaData() {
		if (seqMd == null) {
			for (DataItem item : getDataItems()) {
				if (Sequence.DNA_SEQ_SOURCE_TYPE.equals(item.getSource()
						.getType())) {
					seqMd = new SequenceMetaData(item.getSource().getName(),
							item.getIdentifier());
					seqMd.addDataItem(item);
					break;
				}
			}
		}
		return seqMd;
	}

	public String getAccession() {
		return spec.getAccession();
	}

	public int getGeneticCode() {
		return spec.getGeneticCode();
	}

	public int getLength() {
		return spec.getLength();
	}

	public String getOlnRegexp() {
		return spec.getOlnRegexp();
	}

	public int getType() {
		return spec.getType();
	}

	public boolean isCircular() {
		return spec.isCircular();
	}

	public void setAccession(String accession) {
		spec.setAccession(accession);
	}

	public void setCircular(boolean circular) {
		spec.setCircular(circular);
	}

	public void setGeneticCode(int geneticCode) {
		spec.setGeneticCode(geneticCode);
	}

	public void setLength(int length) {
		spec.setLength(length);
	}

	public void setOlnRegexp(String olnRegexp) {
		spec.setOlnRegexp(olnRegexp);
	}

	public void setType(int type) {
		spec.setType(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #getGenomeInfo()
	 */
	public GenomeInfo getGenomeInfo() {
		return spec.getGenomeInfo();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #setGenomeInfo(uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo)
	 */
	public void setGenomeInfo(GenomeInfo genome) {
		this.spec.setGenomeInfo(genome);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #getDescription()
	 */
	public String getDescription() {
		return spec.getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		spec.setDescription(description);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #getMoleculeType()
	 */
	public String getMoleculeType() {
		return spec.getMoleculeType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentSpecification
	 * #setMoleculeType(java.lang.String)
	 */
	public void setMoleculeType(String moleculeType) {
		spec.setMoleculeType(moleculeType);
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date date) {
		this.updateDate = date;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getVersionedAccession() {
		if (StringUtils.isEmpty(this.versionedAccession)) {
			this.versionedAccession = this.getAccession() + "."
					+ this.getVersion();
		}
		return this.versionedAccession;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public GenomicComponentType getComponentType() {
		return componentType;
	}

	public void setComponentType(GenomicComponentType componentType) {
		this.componentType = componentType;
	}

	public String getMasterAccession() {
		return masterAccession;
	}

	public void setMasterAccession(String masterAccession) {
		this.masterAccession = masterAccession;
	}

	public GenomicComponentDescriptionHandler getDescriptionHandler() {
		return descriptionHandler;
	}

	public void setDescriptionHandler(
			GenomicComponentDescriptionHandler descriptionHandler) {
		this.descriptionHandler = descriptionHandler;
	}

	public void parseComponentDescription() {
		if (getDescriptionHandler() != null) {
			getDescriptionHandler().parseComponentDescription(this);
		}
	}

	public Set<String> getSynonyms() {
		if (synonyms == null) {
			synonyms = new HashSet<String>();
		}
		return synonyms;
	}

	public boolean isCon() {
		return con;
	}

	public void setCon(boolean con) {
		this.con = con;
	}
	
}
