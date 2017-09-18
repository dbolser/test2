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

package uk.ac.ebi.proteome.util.reflection.sub;

/**
 * Used as a test object for reflection work
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class InvokerTestObject {

	private int val = 1;
	public boolean switcher = false;

	public int getVal() {
		return val;
	}

	public int getVal(int addition) {
		return val + addition;
	}

	public int getVal(Integer addition) {
		return val + addition;
	}

	public int getVal(Number addition) {
		return val + addition.intValue();
	}

	public void switchSwitcherTrue() {
		switcher = true;
	}

	public static String echo() {
		return "echo";
	}
}
