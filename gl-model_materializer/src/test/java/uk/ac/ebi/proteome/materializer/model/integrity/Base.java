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

package uk.ac.ebi.proteome.materializer.model.integrity;

import config.IntegrationConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.DatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.genomebuilder.xrefregistry.impl.TableBackedDatabaseReferenceTypeRegistry;
import uk.ac.ebi.proteome.materializer.model.ModelRegistry;
import uk.ac.ebi.proteome.services.ServiceContext;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.sql.impl.LocalSqlService;
import uk.ac.ebi.proteome.services.support.MapServiceProvider;
import uk.ac.ebi.proteome.services.support.ServicesEnum;

/**
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class Base {

	private static IntegrationConfig config;
	private static SqlService sqlService;
	private ModelRegistry registry;

	@BeforeClass
	public static void populateConfig() {
		config = IntegrationConfig.create();
		sqlService = new LocalSqlService();
	}

	@Before
	public void registry() {
		ModelRegistry registry = new ModelRegistry(config.getModelUri(), sqlService);

		ServiceContext fakeContext = new ServiceContext();
		MapServiceProvider provider = new MapServiceProvider();
		provider.setService(ServicesEnum.SQL, sqlService);
		fakeContext.setServiceProvider(provider);
		
		DatabaseReferenceTypeRegistry xrefTypeReg =
			new TableBackedDatabaseReferenceTypeRegistry(fakeContext, config.getModelUri());
		registry.put(DatabaseReferenceTypeRegistry.class, xrefTypeReg);

		this.registry = registry;
	}

	public static IntegrationConfig getConfig() {
		return config;
	}

	public static SqlService getSqlService() {
		return sqlService;
	}

	public ModelRegistry getRegistry() {
		return registry;
	}
}
