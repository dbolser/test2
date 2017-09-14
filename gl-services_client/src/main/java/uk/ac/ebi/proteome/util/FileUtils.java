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
 * File: FileUtils.java
 * Created by: dstaines
 * Created on: Nov 7, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.util;

import java.io.File;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

/**
 * @author dstaines
 *
 */
public class FileUtils {

	private static int SLEEP = 5000;

	private static int RETRY = 3;

	public static boolean fileExists(File file) {
		int attempt = 0;
		while (attempt++ < RETRY) {
			if (file.exists()) {
				return true;
			} else {
				try {
					Thread.sleep(SLEEP);
				} catch (InterruptedException e) {
				}
			}
		}
		return false;

	}

	public static void createParentDirs(String fileName) {
		createParentDirs(new File(fileName));
	}

	public static void createParentDirs(File file) {
		file.getParentFile().mkdirs();
	}

	/**
	 * Find the common root shared by the supplied list of files
	 * @param files file list to analyse
	 * @return common root, or null if not found
	 */
	public static String findCommonRoot(Collection<File> files) {
		String parentDir = null;
		for (File file : files) {
			String parent = file.getParent();
			if (parentDir == null) {
				parentDir = parent;
			}
			if (!parentDir.equals(parent)) {
				int indexOfDiff = StringUtils.indexOfDifference(parentDir,
						parent);

				if(indexOfDiff == 0) {
					// strings have no common root
					parentDir = null;
					break;
				} else if(indexOfDiff>0) {
					// common root found
					parentDir = parentDir.substring(0, indexOfDiff);
					// trim back to the last separator character
					parentDir = parentDir.substring(0, parentDir.lastIndexOf(File.separatorChar));
				} else if(indexOfDiff==-1) {
					// identical
				}
			}
		}
		return parentDir;
	}

}
