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


/**
 * A set of helper methods which return true if the two paramters are
 * equal to each other. It should be used with Equals Methods where object
 * generation is a problem therefore stopping you from using Commons-Lang
 * EqualsBuilder.
 *
 * @author ady
 * @author $Author$
 * @revision $Revision$
 */
public class EqualsHelper {

	public static boolean equal(int one, int two) {
		return (one == two);
	}

	public static boolean equal(long one, long two) {
		return (one == two);
	}

	public static boolean equal(boolean one, boolean two) {
		return (one == two);
	}

	public static boolean equal(Object one, Object two) {
		boolean equal = false;
		if(one == null && two == null) {
			equal = true;
		}
		else if (one == null || two == null) {
			equal = false;
		}
		else {
			//Same ref equals should be delt with in the equals()
			//method of the calling class and not here
			equal = one.equals(two);
		}
		return equal;
	}

	/**
	 * This method should be called before begininng any equals methods. In order
	 * to return true the method:
	 *
	 * <ol>
	 * <li>The two given objects are the same instance using ==. This also means
	 * if both Objects are null then this method will return true (well
	 * techincally they are equal)</li>
	 * <li>Tests that neither object is null</li>
	 * <li>The the two classes from the objects are equal using ==</li>
	 * </ol>
	 *
	 * The boilerplate using this method then becomes:
	 *
	 * <pre>
	 * boolean equals = false;
	 *
	 * if ( EqualsHelper.classEqual(this, obj) ) {
	 *   TargetClass casted = (TargetClass)obj;
	 *
	 *   equals = (
	 *     EqualsHelper.equal(this.getId(), casted.getId()) &&
	 *     EqualsHelper.equal(this.getName(), casted.getName())
	 *   );
	 * }
	 *
	 * return equals;
	 * </pre>
	 *
	 * @param one The first object to test
	 * @param two The second object to test
	 * @return A boolean indicating if the logic agrees that these two
	 * objects are equal at the class level
	 */
	public static boolean classEqual(Object one, Object two) {

		boolean equal = false;

		if(one == two) {
			equal = true;
		}
		else if(one == null || two == null) {
			equal = false;
		}
		else {
			//We can replace this with
			//one.getClass().isInstance(two);
			//However the only advantage is the method version does a null check which
			//we cannot be in because of the first if statement
			equal = (one.getClass() == two.getClass());
		}

		return equal;
	}
}
