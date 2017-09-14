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

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.GenomicComponent;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomicComponentImpl;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.mirror.sequence.Sequence;
import uk.ac.ebi.proteome.mirror.sequence.SequenceFile;
import uk.ac.ebi.proteome.mirror.sequence.SequenceFormat;
import uk.ac.ebi.proteome.mirror.sequence.SequenceInformation;
import uk.ac.ebi.proteome.mirror.sequence.impl.SequenceFileBackedSequence;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Maps from SQL to a genomic component
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class GenomicComponentMapRowMapper extends
		AbstractCollectionMapRowMapper<Persistable<GenomicComponent>> {

	/**
	 * The default format of sequence dates dd-MM-yyyy (- separated UK date
	 * style)
	 */
	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";

	private final Log log = LogFactory.getLog(this.getClass());
	private final DateFormat dateFormat;

	public GenomicComponentMapRowMapper() {
		dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
	}

	public void existingObject(
			Collection<Persistable<GenomicComponent>> currentValue,
			ROResultSet resultSet, int position) throws SQLException {
		Long id = resultSet.getLong(2);
		GenomicComponentImpl gc = mapComponent(resultSet, id);
		gc.setMetaData(mapMetaData(resultSet, 4, gc));
		gc.setSequence(mapSequence(resultSet, 9));
		currentValue
				.add(new SimpleWrapperPersistable<GenomicComponent>(gc, id));
	}

	public GenomicComponentImpl mapComponent(ROResultSet rs, Long id)
			throws SQLException {
		GenomicComponentImpl gc = new GenomicComponentImpl();
		gc.setId(id.longValue());
		gc.setAccession(rs.getString(3));
		gc.setType(rs.getInt(4));
		return gc;
	}

	public GenomicComponentMetaData mapMetaData(ROResultSet rs, int offset,
			GenomicComponentImpl gc) throws SQLException {
		GenomicComponentMetaData mt = new GenomicComponentMetaData();
		mt.setGeneticCode(rs.getInt(offset + 1));
		mt.setAccession(gc.getAccession());
		mt.setLength(rs.getInt(offset + 2));
		mt.setCircular(rs.getBoolean(offset + 3));
		mt.setDescription(rs.getString(offset + 4));
		mt.setType(gc.getType());
		mt.setMoleculeType(rs.getString(offset + 5));
		mt.parseComponentDescription();
		return mt;
	}

	public Sequence mapSequence(ROResultSet rs, int offset) throws SQLException {
		String version = rs.getString(offset + 1);
		Date rawDate = rs.getDate(offset + 2);
		String path = rs.getString(offset + 3);
		String formattedDate = (rawDate == null) ? StringUtils.EMPTY
				: dateFormat.format(rawDate);

		Sequence seq;
		try {
			seq = new SequenceFileBackedSequence(SequenceFile.getSequenceFile(
					SequenceFormat.FASTA, new File(path)));
			seq.getProperties().put(SequenceInformation.PROPERTY_VERSION,
					version);
			if (!StringUtils.EMPTY.equals(formattedDate)) {
				seq.getProperties().put(SequenceInformation.PROPERTY_DATE,
						formattedDate);
			}
		} catch (IOException e) {
			String msg = "Cannot map sequence due to IOException";
			String ioMsg = e.getMessage();
			log.debug(msg, e);
			throw new SQLException(msg
					+ ". Details sent to debug log. Error was: " + ioMsg);
		}

		return seq;
	}

}
