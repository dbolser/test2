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

package uk.ac.ebi.proteome.util;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Just checking up on some situations in the EqualsHelper
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class EqualsHelperTest extends TestCase {

	public void testClassEqual() {
		assertFalse("First param was null & therefore classEqual should return false", EqualsHelper.classEqual(null, ""));
		assertFalse("Second param was null & therefore classEqual should return false", EqualsHelper.classEqual("", null));

		ArrayList<Object> one = new ArrayList<Object>();
		ArrayList<Object> two = one;

		assertTrue("Both lists point to same value therefore are equal", EqualsHelper.classEqual(one, two));

		assertFalse("One was a list, the other was a String but method said true", EqualsHelper.classEqual(one, ""));

		assertTrue("Both were Strings but method said false", EqualsHelper.classEqual("a", ""));

		assertTrue("Both params were null so this should be true", EqualsHelper.classEqual(null, null));
	}

}
