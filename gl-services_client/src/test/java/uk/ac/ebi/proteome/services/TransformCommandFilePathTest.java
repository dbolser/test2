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

/**
 * File: TransformFilePathTest.java
 * Created by: dstaines
 * Created on: Jun 25, 2007
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services;

import java.io.File;

import junit.framework.TestCase;

/**
 * @author dstaines
 *
 */
public class TransformCommandFilePathTest extends TestCase {

	private final static String BASE_DIR = "/the/way/to/base";

	private ServiceContext context = null;

	@Override
	protected void setUp() throws Exception {
		context = new ServiceContext();
		context.setBaseDirectory(BASE_DIR);
	}

	/**
	 * @param arg0
	 */
	public TransformCommandFilePathTest(String arg0) {
		super(arg0);
	}

	public void testHome() throws Exception {
		String input = "mycommand ~/some/file";
		String output = "mycommand " + System.getProperty("user.home")
				+ File.separatorChar + "some/file";
		assertEquals(context.transformCommand(input), output);
	}

	public void testBase() throws Exception {
		String input = "mycommand some/file";
		assertEquals(context.transformCommand(input), input);
	}

	public void testDotBase() throws Exception {
		String input = "mycommand ./some/file";
		String output = "mycommand " + BASE_DIR + File.separatorChar
				+ "some/file";
		assertEquals(context.transformCommand(input), output);
	}

	public void testHomeDotBase() throws Exception {
		String input = "mycommand ./some/file ~/some/other/file";
		String output = "mycommand " + BASE_DIR + File.separatorChar
				+ "some/file " + System.getProperty("user.home")
				+ File.separatorChar + "some/other/file";
		assertEquals(context.transformCommand(input), output);
	}

	public void testBaseWord() throws Exception {
		String input = "mycommand $base_dir$some/file $base_dir$some/other/file";
		String output = "mycommand " + BASE_DIR + File.separatorChar
				+ "some/file " + BASE_DIR + File.separatorChar
				+ "some/other/file";
		assertEquals(context.transformCommand(input), output);
	}

	public void testCommand() throws Exception {
		String input = "./bin/runRunner.sh jobId={0}";
		String output = BASE_DIR + File.separator
				+ "bin/runRunner.sh jobId={0}";
		assertEquals(context.transformCommand(input), output);
	}

	public void testAntCommand() throws Exception {
		String input = "ant -f ./bin/runRunner.sh -DjobId={0} run-job";
		String output = "ant -f " + BASE_DIR + File.separatorChar
				+ "bin/runRunner.sh -DjobId={0} run-job";
		assertEquals(context.transformCommand(input), output);
	}

}
