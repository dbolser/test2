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

package org.ensembl.genomeloader.genomebuilder.xrefregistry;

import static org.ensembl.genomeloader.util.InputOutputUtils.slurpTextClasspathResourceToStringReader;
import static org.ensembl.genomeloader.util.reflection.ReflectionUtils.getResourceAsStreamCompatibleName;

import java.io.Reader;

import org.ensembl.genomeloader.genomebuilder.xrefregistry.impl.XmlDatabaseReferenceTypeRegistry;
import org.ensembl.genomeloader.services.ServiceUncheckedException;

/**
 * Attempting to have a subclass which uses a different classpath location for
 * the XML location.
 * 
 * @author $Author$
 * @author ayates
 * @version $Revision$
 */
public class MockXrefRegistry extends XmlDatabaseReferenceTypeRegistry {

	@Override
	protected Reader getReader() throws ServiceUncheckedException {
		String loc = getResourceAsStreamCompatibleName(this.getClass(), 
				"xrefreg.xml");
		return slurpTextClasspathResourceToStringReader(loc);
	}
	
}
