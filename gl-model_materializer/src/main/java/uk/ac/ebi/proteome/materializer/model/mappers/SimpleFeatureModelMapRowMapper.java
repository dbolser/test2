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

import uk.ac.ebi.proteome.genomebuilder.model.SimpleFeature;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.SimpleFeatureImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

import java.util.Collection;
import java.sql.SQLException;

import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.RichLocation;
import org.biojavax.bio.seq.SimpleRichLocation;

/**
 * Created by IntelliJ IDEA.
 * User: arnaud
 * Date: 11-Jul-2008
 * Time: 17:17:31
 */
public class SimpleFeatureModelMapRowMapper
        extends AbstractCollectionMapRowMapper<Persistable<SimpleFeature>> {

    public void existingObject(Collection<Persistable<SimpleFeature>> currentValues,
                               ROResultSet resultSet, int i)
            throws SQLException {

        SimpleFeatureImpl simpleFeature = new SimpleFeatureImpl();
        
        simpleFeature.setDisplayLabel(resultSet.getString(2));
        simpleFeature.setFeatureType(resultSet.getString(3));
        simpleFeature.addQualifier("analysis",resultSet.getString(3));
        simpleFeature.addQualifier("score",String.valueOf(resultSet.getDouble(7)));

        int min = resultSet.getInt(4);
		int max = resultSet.getInt(5);
		int strandValue = resultSet.getInt(6);

        EntityLocation location = new DelegatingEntityLocation(
			new SimpleRichLocation(
				new SimplePosition(min),
				new SimplePosition(max),
                0,
                RichLocation.Strand.forValue(strandValue)
			)
		);
        simpleFeature.setLocation(location);

        currentValues.add(new SimpleWrapperPersistable<SimpleFeature>(simpleFeature,0));
    }
    
}
