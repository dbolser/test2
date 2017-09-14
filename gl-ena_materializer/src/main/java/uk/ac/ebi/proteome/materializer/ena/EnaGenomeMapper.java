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

package uk.ac.ebi.proteome.materializer.ena;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import javax.xml.ws.handler.MessageContext.Scope;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.genomebuilder.model.GenomeInfo.OrganismNameType;
import uk.ac.ebi.proteome.genomebuilder.model.impl.GenomeInfoImpl;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.MapRowMapper;

/**
 * 
 * Query is expected to be of the form:
 * 
 * select PROTEOME_ID, c.EMBL_TAXID, e.genome_name, p.superregnum_name, p.scope,
 * o.lineage, c.component_id, genome_ac, c.component_name, e.topology,
 * e.seq_length, e.seq_version, c.component_type from proteomes.proteome p join
 * proteomes.ena_collection col using (proteome_id) join proteomes.component c
 * using (proteome_id) join proteomes.embl_genome e using (genome_ac) left join
 * proteomes.organism using (oscode)
 * 
 * Note that postprocessing using PRDB1 is needed for genetic code, seq
 * versioning and expansion of components. Some of this can be done with the XML
 * file though.
 * 
 * @author dstaines
 * 
 */
public class EnaGenomeMapper implements MapRowMapper<String, GenomeMetaData> {

    private static final String ENA_SRC = "ENA";
    private static final int COMPONENT_OFFSET = 7;

    public void existingObject(GenomeMetaData g, ROResultSet resultSet, int position) throws SQLException {
        // decorate with components
        GenomicComponentMetaData md = new GenomicComponentMetaData(ENA_SRC, resultSet.getString(COMPONENT_OFFSET),
                resultSet.getString(COMPONENT_OFFSET + 1), g);
        md.setDescription(resultSet.getString(COMPONENT_OFFSET + 2));
        md.setCircular("C".equals(resultSet.getString(COMPONENT_OFFSET + 3)));
        md.setLength(resultSet.getInt(COMPONENT_OFFSET + 4));
        md.setVersion(resultSet.getString(COMPONENT_OFFSET + 5));
        md.setType(resultSet.getInt(COMPONENT_OFFSET + 6));
        md.setGeneticCode(resultSet.getInt(COMPONENT_OFFSET + 7));
        md.parseComponentDescription();
        g.getComponentMetaData().add(md);
    }

    public String getKey(ROResultSet resultSet) throws SQLException {
        return resultSet.getString(1);
    }

    public Map<String, GenomeMetaData> getMap() {
        return CollectionUtils.createHashMap();
    }

    public GenomeMetaData mapRow(ROResultSet resultSet, int position) throws SQLException {
        // create genome object
        String id = resultSet.getString(1);
        int taxId = resultSet.getInt(2);
        String name = resultSet.getString(3).trim();
        String superregnum = resultSet.getString(4);
        String lineage = resultSet.getString(6);
        GenomeInfoImpl info = new GenomeInfoImpl(id, taxId, name, superregnum);
        info.setOrganismName(OrganismNameType.FULL, name);
        String sqlName = name.replaceAll("[^A-Za-z0-9]+", "_");
        sqlName = sqlName.replaceAll("_+", "_");
        info.setOrganismName(OrganismNameType.SQL, sqlName);
        if (!StringUtils.isEmpty(lineage)) {
            for (String lin : lineage.split(";\\s*")) {
                info.getLineage().add(lin);
            }
            info.getLineage().add(name);
            Collections.reverse(info.getLineage());
        }
        GenomeMetaData g = new GenomeMetaData(info);
        // decorate with component information
        existingObject(g, resultSet, position);
        return g;
    }

}
