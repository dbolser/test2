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

package uk.ac.ebi.proteome.services;

import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.support.ServicesEnum;

/**
 * Core provider of services for the Integr8 system. This is retrieved from
 * {@link ServiceContext} and should be used when constructing the consumers of
 * Services. This forces the programmer into a IoC style of programming which is
 * very test friendly.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface ServiceProvider {

    /**
     * Returns an instance of SqlService. Used for running database commands.
     */
    SqlService getSqlService();

    /**
     * Returns a service for the given eunm. Useful in a meta programming
     * environment
     */
    <T> T getService(ServicesEnum service);

}
