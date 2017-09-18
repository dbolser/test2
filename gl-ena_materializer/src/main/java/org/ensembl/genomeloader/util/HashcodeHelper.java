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

import java.lang.reflect.Array;

/**
 * Contains helper methods for generating a HashCode without having to resort to
 * the commons lang hashcode builders.
 *
 * The code was taken from http://www.javapractices.com/Topic28.cjp which
 * adhers to the rules in Effective Java pg 36 - Chapter 3 Item 8
 *
 * Example where the property name is a String and the property age is an int
 *
 * <pre>
 * public int hashCode {
 *   int result = HashcodeHelper.SEED;
 *   result = HashcodeHelper.hash(result, this.getName());
 *   result = HashcodeHelper.hash(result, this.getAge());
 *   return result;
 * }
 * </pre>
 *
 * @author ady
 * @author $Author$
 * @version $Revision$
 */
public class HashcodeHelper {

	/**
	 * An initial value for a <code>hashCode</code>, to which is added
	 * contributions from fields. Using a non-zero value decreases collisons of
	 * <code>hashCode</code> values.
	 */
	public static final int SEED = 17;

	/**
	 * The prime number used to multiply any calculated hascode seed by
	 *
	 * i.e. result = DEFAULT_PRIME_NUMBER*result + c
	 *
	 * Where result is the result of the previous calculation (at first this will
	 * be seed) and c is the calculated int to add to result
	 */
	public static final int DEFAULT_PRIME_NUMBER = 37;

	/**
	 * booleans
	 */
	public static int hash(int aSeed, boolean aBoolean) {
		return firstTerm(aSeed) + (aBoolean ? 1 : 0);
	}

	/**
	 * chars
	 */
	public static int hash(int aSeed, char aChar) {
		return firstTerm(aSeed) + (int) aChar;
	}

	/**
	 * Used for ints, bytes and shorts via implicit conversion
	 */
	public static int hash(int aSeed, int aInt) {
		return firstTerm(aSeed) + aInt;
	}

	/**
	 * longs
	 */
	public static int hash(int aSeed, long aLong) {
		return firstTerm(aSeed) + (int) (aLong ^ (aLong >>> 32));
	}

	/**
	 * floats
	 */
	public static int hash(int aSeed, float aFloat) {
		return hash(aSeed, Float.floatToIntBits(aFloat));
	}

	/**
	 * doubles
	 */
	public static int hash(int aSeed, double aDouble) {
		//This is handled via the seed(int,long) method
		return hash(aSeed, Double.doubleToLongBits(aDouble));
	}

	/**
	 * <code>aObject</code> is a possibly-null object field, and possibly an
	 * array.
	 *
	 * If <code>aObject</code> is an array, then each element may be a primitive
	 * or a possibly-null object.
	 */
	public static int hash(int aSeed, Object aObject) {
		int result = aSeed;
		if (aObject == null) {
			result = hash(result, 0);
		}
		else if (!isArray(aObject)) {
			result = hash(result, aObject.hashCode());
		}
		else {
			int length = Array.getLength(aObject);
			for (int idx = 0; idx < length; ++idx) {
				Object item = Array.get(aObject, idx);
				// recursive call!
				result = hash(result, item);
			}
		}
		return result;
	}

	private static int firstTerm(int aSeed) {
		return DEFAULT_PRIME_NUMBER * aSeed;
	}

	private static boolean isArray(Object aObject) {
		return aObject.getClass().isArray();
	}
}
