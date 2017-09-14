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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.StopWatch;

import junit.framework.TestCase;
import uk.ac.ebi.proteome.util.InputOutputUtils;

/**
 * Attempts to check that the current 64 implementation operates correctly (
 * well is in a condition that is equivalent to the genome reviews one).
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class Crc64Test extends TestCase {

	public void testChecksum() {
		StreamingChecksum crc64 = ChecksumProvider.getCrc64();
		String originalChecksum = crc64.toString();

		String expected = getExpected();
		crc64.process(getInputStream(getInput()));
		String actual = crc64.toString();

		assertEquals("StreamingChecksum was not as expected for input: "+getInput(), expected, actual);

		assertResettedChecksum(originalChecksum, crc64);
	}

	public void testSplitInputChecksum() {
		StreamingChecksum crc64 = ChecksumProvider.getCrc64();
		String input = getInput();

		//Substrings up to H
		String inputOne = input.substring(0, input.indexOf('H'));
		//Does the rest
		String inputTwo = input.substring(input.indexOf('H'));

		crc64.process(getInputStream(inputOne));
		crc64.process(getInputStream(inputTwo));

		assertEquals("StreamingChecksum did not match split input for "+getInput(),
			getExpected(), crc64.toString());
	}

	public void testStringChecksum() {
		StreamingChecksum crc64 = ChecksumProvider.getCrc64();
		crc64.process(getInput());
		assertEquals("StreamingChecksum did not match String input for "+getInput(),
			getExpected(), crc64.toString());
	}

	public void testFileChecksum() {
		File file = null;
		try {
			file = File.createTempFile("crc64", "test");
			file.deleteOnExit();
			FileUtils.writeStringToFile(file, getInput(), "UTF8");
		}
		catch (IOException e) {
			e.printStackTrace();
			fail("Cannot proceed because of IOException");
		}

		StreamingChecksum crc64 = ChecksumProvider.getCrc64();
		crc64.process(file);
		assertEquals("StreamingChecksum did not match file written input for "+getInput(),
			getExpected(), crc64.toString());
	}

	/**
	 * Uses the Python cookbook's test case
	 */
	public void testPythonCookbookCrc64() {
		StreamingChecksum crc64 = ChecksumProvider.getCrc64();
		String input = "IHATEMATH";
		crc64.process(input);
		String expected = "E3DCADD69B01ADD1";
		String actual = crc64.toString();
		assertEquals("StreamingChecksum did not match for input "+input, expected, actual);
	}

	public void testLargeInput() {
		InputStream is = InputOutputUtils.openClasspathResource(this.getClass(),
			"EMBL_SEQ_CP000408.seq.raw.gz");
		try {
			is = new BufferedInputStream(new GZIPInputStream(is));
			StreamingChecksum crc64 = ChecksumProvider.getCrc64();
			StopWatch watch = new StopWatch();
			watch.start();
			crc64.process(is);
			watch.stop();

			assertEquals("StreamingChecksum not as expected for large input",
				"37f284c174170a13".toUpperCase(), crc64.toString());

			long timeTaken = watch.getTime();
			if(timeTaken > 550) {
				fail("Time taken to process was greater than 550ms (wallclock). " +
					"Check implementation/os load");
			}
		}
		catch(IOException e) {
			e.printStackTrace();
			fail("Could not create GZIP input stream");
		}
		finally {
			InputOutputUtils.closeQuietly(is);
		}
	}

	protected InputStream getInputStream(String input) {
		ByteArrayInputStream is = new ByteArrayInputStream(input.getBytes());
		return is;
	}

	protected String getInput() {
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	}

	protected String getExpected() {
		return "068fe86ba35e5de4".toUpperCase();
	}

	private void assertResettedChecksum(String originalChecksum, StreamingChecksum checksum) {
		checksum.reset();
		String actual = checksum.toString();
		assertEquals("Resetted checksum was not the same as the input",
			originalChecksum, actual);
	}
}
