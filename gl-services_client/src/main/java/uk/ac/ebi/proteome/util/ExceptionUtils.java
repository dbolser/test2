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
 * File: ExceptionUtils.java
 * Created by: dstaines
 * Created on: Apr 15, 2008
 * CVS:  $$
 */
package uk.ac.ebi.proteome.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Utility class for dealing with exceptions
 *
 * @author dstaines
 *
 */
public class ExceptionUtils {

	/**
	 * Walk down to root cause of {@link Throwable}
	 *
	 * @param e
	 *            throwable object o examine
	 * @return root cause, or current throwable if no cause
	 */
	public static Throwable getRootCause(Throwable e) {
		if (e.getCause() != null) {
			return getRootCause(e.getCause());
		} else {
			return e;
		}
	}

	private static Pattern oraCode = Pattern.compile("ORA-[0-9]+");

	public static List<String> extractAllOracleCodes(Throwable e) {
		Throwable cause = getRootCause(e);
		cause.getMessage();
		List<String> matches = CollectionUtils.createArrayList();
		Matcher mat = oraCode.matcher(e.getMessage());
		while (mat.find()) {
			matches.add(mat.group());
		}
		return matches;
	}

	public static String extractRootOracleCode(Throwable e) {
		String code = null;
		List<String> codes = extractAllOracleCodes(e);
		if (codes.size() > 0)
			code = codes.get(codes.size()-1);
		return code;
	}

	public static String extractOracleCode(Throwable e) {
		String code = null;
		List<String> codes = extractAllOracleCodes(e);
		if (codes.size() > 0)
			code = codes.get(0);
		return code;
	}

}
