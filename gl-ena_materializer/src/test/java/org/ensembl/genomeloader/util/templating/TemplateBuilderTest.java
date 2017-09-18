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

package org.ensembl.genomeloader.util.templating;

import java.util.HashMap;
import java.util.Map;

import org.ensembl.genomeloader.util.UtilUncheckedException;
import org.ensembl.genomeloader.util.templating.TemplateBuilder;

import junit.framework.TestCase;

/**
 * Tests the ability to generate templates using the template builder
 * 
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class TemplateBuilderTest extends TestCase {

	public void testGenerate() {
		String template = "Hello $one$ $$two$$";
		
		TemplateBuilder builder = new TemplateBuilder(template);
		builder.addPlaceHolder("one", "there");
		Map<String,Object> input = new HashMap<String, Object>();
		input.put("$two$", 1);
		builder.addPlaceHolders(input);
		
		String expected = "Hello there 1";
		String actual = builder.generate();
		
		assertEquals("Generated String was not the same as expected", expected, actual);
	}
	
	public void testPerlArrayToHash() {
		String template = "$1$ $2$";
		String expected = "hello there";
		TemplateBuilder builder = new TemplateBuilder(template);
		builder.addPlaceHolders(1, "hello", 2, "there");
		String actual = builder.generate();
		assertEquals("Generated String not as expected", expected, actual);
		
		try {
			builder.clearPlaceholders();
			builder.addPlaceHolders(1, "hello", 2);
			fail("Builder was given an odd numbered set of placeholders to the " +
					"Perlish method. This should have raised a UtilUncheckedException");
		}
		catch(UtilUncheckedException e) {
			//Okay
		}
	}
	
	public void testStaticBuilder() {
		String expected = "hello sir!";
		String actual = TemplateBuilder.template("hello $sal$!", "sal", "sir");
		assertEquals("Generated String not as expected", expected, actual);
	}

	public void testNulls() {
		String expected = "hello null!";
		String actual = TemplateBuilder.template("hello $sal$!", "sal", null);
		assertEquals("Generated String not as expected", expected, actual);

		expected = "hello sir!";
		actual = TemplateBuilder.template("hello $null$!", null, "sir");
		assertEquals("Generated String not as expected", expected, actual);
	}

	public void testStringValueOfFormatting() {
		String expected = "123456789";
		String actual = String.valueOf(123456789);
		assertEquals("Integer parsing should include no commas", expected, actual);
	}
}
