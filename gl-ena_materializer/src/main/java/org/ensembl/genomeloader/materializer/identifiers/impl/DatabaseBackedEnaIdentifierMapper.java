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

package org.ensembl.genomeloader.materializer.identifiers.impl;

import org.ensembl.genomeloader.materializer.EnaGenomeConfig;
import org.ensembl.genomeloader.materializer.identifiers.EnaIdentifierMapper;
import org.ensembl.genomeloader.services.sql.SqlService;
import org.ensembl.genomeloader.util.sql.SqlLib;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplate;
import org.ensembl.genomeloader.util.sql.SqlServiceTemplateImpl;
import org.ensembl.genomeloader.util.sql.TransactionalDmlHolder;

public class DatabaseBackedEnaIdentifierMapper implements EnaIdentifierMapper {

    public DatabaseBackedEnaIdentifierMapper(EnaGenomeConfig config, SqlService srv) {
        this(new SqlServiceTemplateImpl(config.getIdUri(), srv));
    }

    private final SqlServiceTemplate srv;
    private final SqlLib lib;

    public DatabaseBackedEnaIdentifierMapper(SqlServiceTemplate srv) {
        this.srv = srv;
        this.lib = new SqlLib("/uk/ac/ebi/proteome/materializer/ena/identifiers/sql.xml");
    }

    public void createSchema(boolean clean) {
        if (clean) {
            // drop tables
            srv.executeSql(lib.getQuery("dropIdTable"));
        }
        // create tables
        srv.executeSql(lib.getQuery("createIdTable"));
    }

    public String mapIdentifier(IdentifierType type, String identifier) {
        // take the easy way out and do an insert ignore
        TransactionalDmlHolder h = new TransactionalDmlHolder();
        h.addStatement(lib.getQuery("insertId"), new Object[] { identifier, type.name() });
        srv.executeTransactionalDml(h);
        // and now a select
        return idToString(type,
                srv.queryForDefaultObject(lib.getQuery("findId"), Integer.class, identifier, type.name()));
    }

    private String idToString(IdentifierType type, int id) {
        return String.format(ENA_ID_STEM + type.getInitial() + "%011d", id);
    }

}
