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
public class TransformFilePathTest extends TestCase {

	private final static String BASE_DIR = "/the/way/to/base";

	private final static String TEST_FILE = "some/lovely/file";

	private ServiceContext context = null;

	@Override
	protected void setUp() throws Exception {
		context = new ServiceContext();
		context.setBaseDirectory(BASE_DIR);
	}

	/**
	 * @param arg0
	 */
	public TransformFilePathTest(String arg0) {
		super(arg0);
	}

	public void testHome() throws Exception {
		String input = "~/" + TEST_FILE;
		String output = System.getProperty("user.home") + File.separatorChar + TEST_FILE;
		assertEquals(context.transformFilePath(input), output);
	}

	public void testBase() throws Exception {
		String input = TEST_FILE;
		String output = BASE_DIR + File.separatorChar + TEST_FILE;
		assertEquals(context.transformFilePath(input), output);
	}

	public void testDotBase() throws Exception {
		String input = "./" + TEST_FILE;
		String output = BASE_DIR + File.separatorChar + TEST_FILE;
		assertEquals(context.transformFilePath(input), output);
	}

	public void testMidHome() throws Exception {
		String input = "~dstaines/" + TEST_FILE;
		assertEquals(context.transformFilePath(input), input);
	}

	public void testMidBase() throws Exception {
		String input = " "+TEST_FILE;
		assertEquals(context.transformFilePath(input), input);
	}

	public void testMidDotBase() throws Exception {
		String input = "/somewhere/else/" +"./" + TEST_FILE;
		assertEquals(context.transformFilePath(input), input);
	}

	public void testBaseHolder() throws Exception {
		String input = "$base_dir$" + TEST_FILE;
		String output = BASE_DIR + File.separatorChar + TEST_FILE;
		assertEquals(context.transformFilePath(input), output);
	}

}
