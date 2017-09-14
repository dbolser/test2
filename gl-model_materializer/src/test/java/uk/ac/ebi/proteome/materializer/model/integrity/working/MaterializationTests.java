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

package uk.ac.ebi.proteome.materializer.model.integrity.working;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Used for running the 'is this materialization working' test suites. They're
 * not very good unit tests because of their depenedncy on a working model
 * database
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
	ModelFinding.class
	})
public class MaterializationTests {
}
