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

package uk.ac.ebi.proteome.materializer.misc;

import static uk.ac.ebi.proteome.util.reflection.ReflectionUtils.getResourceAsStreamCompatibleName;
import uk.ac.ebi.proteome.registry.Registry;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplate;
import uk.ac.ebi.proteome.util.sql.SqlServiceTemplateImpl;
import uk.ac.sanger.cgp.dbcon.sqllibs.SqlLib;
import uk.ac.sanger.cgp.dbcon.sqllibs.SqlLibFacade;

/**
 * Provides some core methods & defaults when you are wanting a new registry to
 * produce things like a sql service template, uris & default services.
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractDbRegistry extends Registry {

	public AbstractDbRegistry(String uri, ServiceContext context) {
		this(uri, context.getSqlService());
	}

	public AbstractDbRegistry(String uri, SqlService service) {
		super();
		put(SqlLib.class, new SqlLibFacade(getSqlLocation()));
		put(SqlServiceTemplate.class, new SqlServiceTemplateImpl(uri, service));
		put(SqlService.class, service);
	}

	/**
	 * Reports location of dbcon sqllib xml file. Currently is the extending
	 * class' package plus sql.xml. Must be a resource as stream compatable
	 * name.
	 */
	protected String getSqlLocation() {
		return getResourceAsStreamCompatibleName(this.getClass(), "sql.xml");
	}
}
