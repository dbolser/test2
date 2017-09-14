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

import uk.ac.ebi.proteome.genomebuilder.model.RepeatRegion;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.RepeatRegionImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.util.Collection;
import java.sql.SQLException;

import org.biojavax.bio.seq.SimpleRichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.RichLocation;

/**
 * Created by IntelliJ IDEA.
 * User: arnaud
 * Date: 10-Jun-2008
 * Time: 17:41:32
 */
public class RepeatRegionModelMapRowMapper
        extends AbstractCollectionMapRowMapper<Persistable<RepeatRegion>> {

    public void existingObject(Collection<Persistable<RepeatRegion>> currentValues, ROResultSet resultSet, int i)
            throws SQLException {

        RepeatRegionImpl repeatRegion = new RepeatRegionImpl();
        RepeatRegionImpl.RepeatUnitImpl repeatUnit = new RepeatRegionImpl.RepeatUnitImpl();
        repeatRegion.setRepeatUnit(repeatUnit);
        
        repeatRegion.setAnalysis(resultSet.getString(5));
		int min = resultSet.getInt(6);
		int max = resultSet.getInt(7);
		int strandValue = resultSet.getInt(10);

        int repeatStart = resultSet.getInt(8);
        int repeatEnd   = resultSet.getInt(9);

        repeatRegion.setRepeatStart(repeatStart);
        repeatRegion.setRepeatEnd(repeatEnd);

        double score = resultSet.getDouble(11);


        EntityLocation location = new DelegatingEntityLocation(
			new SimpleRichLocation(
				new SimplePosition(min),
				new SimplePosition(max),
                0,
                RichLocation.Strand.forValue(strandValue)
			)
		);
        repeatRegion.setLocation(location);
        repeatRegion.setScore(score);

        String repeatName = resultSet.getString(2);
        String repeatClass = resultSet.getString(3);
        String repeatType = resultSet.getString(4);
        String repeatConsensus = resultSet.getString(12);

        repeatUnit.setRepeatName(repeatName);
        repeatUnit.setRepeatClass(repeatClass);
        repeatUnit.setRepeatType(repeatType);
        repeatUnit.setRepeatConsensus(repeatConsensus);

        currentValues.add(new SimpleWrapperPersistable<RepeatRegion>(repeatRegion,0));
    }
}
