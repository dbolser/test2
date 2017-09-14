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

package uk.ac.ebi.proteome.genomebuilder.xrefregistry;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReferenceType;

/**
 * Used to test the current state of the Xml based registry for 
 * database types. Also makes sure we have efficent methods of lookup working
 * 
 * @author $Author$
 * @author ayates
 * @version $Revision$
 */
public class XmlRegTest {
	
	private DatabaseReferenceTypeRegistry registry;

	@Before	public void createRegistry() {
		registry = new MockXrefRegistry();
	}
	
	@Test public void contents() {
		assertEquals("Number of types expected", 4, registry.getAllTypes().size());
		assertEquals("Number of uniprot types", 1, 
				registry.getTypesForName("UniProtKB").size());
		assertEquals("Number of EMBL types", 2, 
				registry.getTypesForName("EMBL").size());
	}
	
	@Test public void byId() {
		int id = 1;
		DatabaseReferenceType type = registry.getType(id);
		assertEquals("Type id", id, type.getId());
		assertEquals("Type name", "EMBL (CDS)", type.getDisplayName());
	}
	
	@Test(timeout=100) public void stressIdFetch() {
		int id = 1;
		for(int i=0; i< 500000; i++) {
			DatabaseReferenceType type = registry.getType(id);
			assertEquals("Type name", "EMBL (CDS)", type.getDisplayName());
		}
	}
}
