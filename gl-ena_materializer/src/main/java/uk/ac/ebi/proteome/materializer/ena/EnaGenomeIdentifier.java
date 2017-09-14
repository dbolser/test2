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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.genomebuilder.metadata.GenomeMetaData;
import uk.ac.ebi.proteome.genomebuilder.metadata.GenomicComponentMetaData;
import uk.ac.ebi.proteome.materializer.ena.impl.IdentificationUncheckedException;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.ROResultSet;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.sql.RowMapper;
import uk.ac.ebi.proteome.util.sql.SqlLib;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;

public class EnaGenomeIdentifier {

    protected final EnaGenomeMapper mapper;
    protected final SqlServiceTemplate protSrv;
    protected final SqlServiceTemplate enaSrv;
    protected final SqlLib sqlLib;
    private Log log;

    protected Log getLog() {
        if (log == null) {
            log = LogFactory.getLog(this.getClass());
        }
        return log;
    }

    public EnaGenomeIdentifier(ServiceContext context, EnaGenomeConfig config) {
        this(new SqlServiceTemplateImpl(config.getProtUri(), context),
                new SqlServiceTemplateImpl(config.getEnaUri(), context));
    }

    public EnaGenomeIdentifier(SqlServiceTemplate protSrv, SqlServiceTemplate enaSrv) {
        this.protSrv = protSrv;
        this.enaSrv = enaSrv;
        this.mapper = new EnaGenomeMapper();
        this.sqlLib = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/sql.xml");
    }

    protected void expandComponents(GenomeMetaData genome) {
        List<GenomicComponentMetaData> comps = CollectionUtils.createArrayList();
        for (GenomicComponentMetaData md : genome.getComponentMetaData()) {
            switch (md.getType()) {
            case 5:
                comps.addAll(expandComponent("expandType5Component", md));
                break;
            case 8:
                comps.addAll(expandComponent("expandType8Component", md));
                break;
            default:
                comps.add(md);
                break;
            }
        }
        genome.getComponentMetaData().clear();
        Set<String> acs = CollectionUtils.createHashSet();
        for (GenomicComponentMetaData comp : comps) {
            if (acs.contains(comp.getAccession())) {
                throw new IdentificationUncheckedException("Accession " + comp.getAccession() + " for genome "
                        + genome.getName() + " (id " + genome.getId() + ") found more than once");
            }
            acs.add(comp.getAccession());
            genome.getComponentMetaData().add(comp);
        }

    }

    protected Collection<GenomicComponentMetaData> expandComponent(String queryName,
            final GenomicComponentMetaData md) {
        RowMapper<GenomicComponentMetaData> cMapper = new RowMapper<GenomicComponentMetaData>() {
            public GenomicComponentMetaData mapRow(ROResultSet resultSet, int position) throws SQLException {

                GenomicComponentMetaData newMd = new GenomicComponentMetaData();
                newMd.setAccession(resultSet.getString(1));
                newMd.setVersion(resultSet.getString(2));
                newMd.setLength(resultSet.getInt(3));
                newMd.setCircular(md.isCircular());
                newMd.setIdentifier(md.getIdentifier() + position);
                newMd.setGeneticCode(md.getGeneticCode());
                newMd.setGenomeInfo(md.getGenomeInfo());
                String description = resultSet.getString(4);
                String category = resultSet.getString(5);// ? needed
                String genomeDes = resultSet.getString(6);
                String chromosome = resultSet.getString(7); // ? needed
                if (!StringUtils.isEmpty(genomeDes)) {
                    newMd.setDescription(genomeDes);
                    newMd.parseComponentDescription();
                } else {
                    newMd.setDescription(description);
                    newMd.parseComponentDescription();
                }
                newMd.setMasterAccession(md.getAccession());
                return newMd;
            }
        };
        List<GenomicComponentMetaData> exComps = enaSrv.queryForList(sqlLib.getQuery(queryName), cMapper,
                getComponentRoot(md));
        if (exComps.size() == 0) {
            getLog().warn(
                    "No components found after expansion of type " + md.getType() + " component " + md.getAccession());
        }
        return exComps;
    }

    private String getComponentRoot(final GenomicComponentMetaData md) {
        return md.getAccession().substring(0, 6) + "%";
    }

    public Collection<GenomeMetaData> getAllMetaData(Object... params) {
        Collection<GenomeMetaData> gs = protSrv.queryForMap(sqlLib.getQuery("getGenomes"), mapper).values();
        for (GenomeMetaData g : gs) {
            expandComponents(g);
        }
        return gs;
    }

    public GenomeMetaData getMetaDataForIdentifier(String identifier, Object... params) {
        GenomeMetaData g = CollectionUtils.getFirstElement(protSrv
                .queryForMap(sqlLib.getQuery("getGenomes") + sqlLib.getQuery("pidClause"), mapper, identifier).values(),
                null);
        if (g != null) {
            expandComponents(g);
        }
        return g;
    }

}
