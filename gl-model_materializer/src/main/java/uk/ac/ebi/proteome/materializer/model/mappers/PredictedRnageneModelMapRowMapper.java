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

import uk.ac.ebi.proteome.genomebuilder.model.Rnagene;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.impl.RnageneImpl;
import uk.ac.ebi.proteome.genomebuilder.model.impl.DelegatingEntityLocation;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.persistence.Persistable;
import uk.ac.ebi.proteome.persistence.persistables.SimpleWrapperPersistable;
import uk.ac.ebi.proteome.materializer.model.abstracts.AbstractCollectionMapRowMapper;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.biojavax.bio.seq.SimpleRichLocation;
import org.biojavax.bio.seq.SimplePosition;
import org.biojavax.bio.seq.RichLocation;

/**
 * @author $Author$
 * @version $Revision$
 */
public class PredictedRnageneModelMapRowMapper extends AbstractCollectionMapRowMapper<Persistable<Rnagene>>
{
	public void existingObject(Collection<Persistable<Rnagene>> currentValue,
		ROResultSet resultSet, int position) throws SQLException {

/*
ID	ANALYSIS    BIOTYPE PSEUDOGENE  NAME	    DESCRIPTION                     MIN     MAX     STRANDN
1	tRNAscan	tRNA	Y	        tRNA-Pseudo	tRNA-Pseudo for anticodon CCG	200265	200336	-1
*/

        currentValue.add(new SimpleWrapperPersistable<Rnagene>(makeRnagene(resultSet),resultSet.getString(1)));
	}

    protected Rnagene makeRnagene(ROResultSet resultSet) throws SQLException {
        RnageneImpl rnagene = new RnageneImpl();
        rnagene.setAnalysis(resultSet.getString(2));
        rnagene.setBiotype(resultSet.getString(3));
        String pseudogene = resultSet.getString(4);
        rnagene.setPseudogene(pseudogene.equals("Y"));
        rnagene.setName(resultSet.getString(5));
        rnagene.setDescription(resultSet.getString(6));
		int min = resultSet.getInt(7);
		int max = resultSet.getInt(8);
		int strandValue = resultSet.getInt(9);

		EntityLocation location = new DelegatingEntityLocation(
			new SimpleRichLocation(
				new SimplePosition(min),
				new SimplePosition(max),
                0,
                RichLocation.Strand.forValue(strandValue)
			)
		);
        rnagene.setLocation(location);
        return rnagene;
    }
}
