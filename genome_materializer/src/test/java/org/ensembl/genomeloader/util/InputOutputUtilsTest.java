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

package org.ensembl.genomeloader.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.ensembl.genomeloader.util.reflection.ReflectionUtils;

import junit.framework.TestCase;

/**
 * Used for testing the InputOutputUtils object
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class InputOutputUtilsTest extends TestCase {

	private File tmpGzipFile = null;

	@Override
	protected void setUp() throws Exception {
		tmpGzipFile = File.createTempFile("sc_gzip-test", "tmp");
		tmpGzipFile.deleteOnExit();
	}

	@Override
	protected void tearDown() throws Exception {
		tmpGzipFile.delete();
	}

	/**
	 * Testing if basic gzipping works
	 */
	public void testCopyFromGzippedClasspathToFileSystem() {
		String resource = ReflectionUtils.getResourceAsStreamCompatibleName(
				this.getClass(), "test.gz");

		InputOutputUtils.copyFromGzippedClasspathToFileSystem(resource, tmpGzipFile);

		String expected = "TEST-GZIP";
		String actual = null;
		try {
			actual = IOUtils.toString(new FileReader(tmpGzipFile)).trim();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			fail("Cannot find temp file");
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Cannot read temp file");
		}

		assertEquals("The contents of the streamed temporary file is not " +
				"the same as expected", expected, actual);
	}

	public void testReadingClasspathResource() {
		String resource = ReflectionUtils.getResourceAsStreamCompatibleName(
				this.getClass(), "test.txt");

		String expected = "TEST";
		String actual = InputOutputUtils.slurpTextClasspathResourceToString(resource);

		assertEquals("The contents of the streamed file is not " +
				"the same as expected", expected, actual);
	}

}
