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

package uk.ac.ebi.proteome.util.checksums;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;

/**
 * Represents a checksum processor. An instance of a checksum should be
 * independant from any other instances apart from any table checksum data
 * they require.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public interface StreamingChecksum extends  java.util.zip.Checksum{

	/**
	 * Give the checksum generator an InputStream to derive bytes from
	 */
	void process(InputStream is);

	/**
	 * Generate the checksum based on an in-memory String
	 */
	void process(String string);

	/**
	 * Generate the checksum based on a file (will open & close resources)
	 */
	void process(File file);

	/**
	 * Generate the checksum based on reader input
	 */
	void process(Reader reader);

}
