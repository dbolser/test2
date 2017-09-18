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

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.proteome.util.collections.CollectionUtils;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;

/**
 * Contains useful methods for dealing with Input/Output opertations
 * 
 * @author ayates
 * @author $Author$
 * @version $Version$
 */
public class InputOutputUtils {

	private static Log log = LogFactory.getLog(InputOutputUtils.class);

	private static Log getLog() {
		return log;
	}

	public static void close(Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				getLog().error("Could not close " + closeable.getClass(), e);
			}
		}
	}

	/**
	 * Performs the same function as the commons io version but also performs
	 * logging of readers which threw an exception when trying to close them and
	 * logs them at error level
	 */
	public static void closeQuietly(Reader reader) {
		close(reader);
	}

	/**
	 * Equivalent of {@link #closeQuietly(Writer)} but for {@link Writer}
	 * extending classes
	 */
	public static void closeQuietly(Writer writer) {
		close(writer);
	}

	/**
	 * Performs the same function as the commons io version but also performs
	 * logging of streams which threw an exception when trying to close them and
	 * logs them at error level
	 */
	public static void closeQuietly(InputStream is) {
		close(is);
	}

	/**
	 * Equivalent of {@link #closeQuietly(InputStream)} but for
	 * {@link OutputStream} extending classes
	 */
	public static void closeQuietly(OutputStream os) {
		close(os);
	}

	/**
	 * Similar to {@link #openClasspathResource(String)} but this uses the given
	 * class to base the package name off. The resource should be just the
	 * resource name
	 */
	public static InputStream openClasspathResource(Class<?> clazz,
			String resource) throws UtilUncheckedException {
		String target = ReflectionUtils.getResourceAsStreamCompatibleName(
				clazz, resource);
		return openClasspathResource(target);
	}

	/**
	 * Attempts to return a classpath resource. It is the responsibility of the
	 * calling class to correctly close this input stream. Use
	 * {@link #closeQuietly(InputStream)} for an easy implementation of closing
	 * input streams.
	 * 
	 * @param resource
	 *            The resource to find. Must be '/package/file.name'
	 * @return The input stream found
	 * @throws UtilUncheckedException
	 *             Thrown if the input stream was null i.e. the resource name
	 *             looked for did not exist
	 */
	public static InputStream openClasspathResource(String resource)
			throws UtilUncheckedException {
		return openClasspathResource(resource, true);
	}

	/**
	 * Allows optional opening of a resource with or without buffering
	 */
	private static InputStream openClasspathResource(String resource,
			boolean buffered) {
		InputStream is = InputOutputUtils.class.getResourceAsStream(resource);
		if (is == null) {
			throw new UtilUncheckedException("Could not find resource "
					+ resource);
		}
		return (buffered) ? new BufferedInputStream(is) : is;
	}

	/**
	 * Attempts to return a gzipped classpath resource. It is the responsibility
	 * of the calling class to correctly close this input stream. Use
	 * {@link #closeQuietly(InputStream)} for an easy implementation of closing
	 * input streams.
	 * 
	 * @param resource
	 *            The resource to find. Must be '/package/file.name'
	 * @return The input stream found
	 * @throws UtilUncheckedException
	 *             Thrown if the input stream was null i.e. the resource name
	 *             looked for did not exist
	 */
	public static InputStream openGzippedClasspathResource(String resource)
			throws UtilUncheckedException {
		try {
			return new GZIPInputStream(openClasspathResource(resource, false));
		} catch (IOException e) {
			throw new UtilUncheckedException("Could not unzip resource "
					+ resource);
		}
	}

	/**
	 * Provides an easy resource to String slurp method
	 */
	public static String slurpTextClasspathResourceToString(String resource)
			throws UtilUncheckedException {
		return slurpTextClasspathResourceToString(resource, false);
	}

	/**
	 * Provides an easy resource to String slurp method
	 */
	public static String slurpGzippedTextClasspathResourceToString(
			String resource) throws UtilUncheckedException {
		return slurpTextClasspathResourceToString(resource, true);
	}

	private static String slurpTextClasspathResourceToString(String resource,
			boolean gzipped) throws UtilUncheckedException {
		String output = StringUtils.EMPTY;
		InputStream is = null;
		try {
			if (!gzipped) {
				is = openClasspathResource(resource);
			} else {
				is = openGzippedClasspathResource(resource);
			}
			output = IOUtils.toString(is);
		} catch (IOException e) {
			throw new UtilUncheckedException(
					"IOException detected whilst streaming resource "
							+ resource, e);
		} finally {
			closeQuietly(is);
		}
		return output;
	}

	/**
	 * For the given resource path this method will stream the resource from the
	 * current class path and pass back a StringReader holding the content of
	 * the resource.
	 */
	public static StringReader slurpTextClasspathResourceToStringReader(
			String resource) throws UtilUncheckedException {
		String slurp = slurpTextClasspathResourceToString(resource);
		return new StringReader(slurp);
	}

	public static StringReader slurpGzippedTextClasspathResourceToStringReader(
			String resource) throws UtilUncheckedException {
		String slurp = slurpGzippedTextClasspathResourceToString(resource);
		return new StringReader(slurp);
	}

	/**
	 * Takes a resource on the classpath and copies it out to the file system
	 * 
	 * @param classpathResource
	 *            The classpath resource as defined as /path
	 * @param outputFile
	 *            The output location for the file
	 */
	public static void copyFromClasspathToFileSystem(String classpathResource,
			File outputFile) {
		InputStream classpathInputStream = openClasspathResource(classpathResource);
		try {
			copyInputStreamToFileSystem(classpathInputStream, outputFile);
		} finally {
			closeQuietly(classpathInputStream);
		}
	}

	/**
	 * Takes a gzipped resource on the classpath, unzips and copies it out to
	 * the file system
	 * 
	 * @param classpathResource
	 *            The classpath resource as defined as /path
	 * @param outputFile
	 *            The output location for the file
	 */
	public static void copyFromGzippedClasspathToFileSystem(
			String classpathResource, File outputFile) {
		InputStream classpathInputStream = openGzippedClasspathResource(classpathResource);
		try {
			copyInputStreamToFileSystem(classpathInputStream, outputFile);
		} finally {
			closeQuietly(classpathInputStream);
		}
	}

	/**
	 * Takes an already open input stream and copies to content to the file
	 * system at the specified point
	 * 
	 * @param stream
	 *            The stream to copy from
	 * @param outputFile
	 *            The file output location
	 */
	public static void copyInputStreamToFileSystem(InputStream stream,
			File outputFile) {
		OutputStream output = null;

		try {
			output = new FileOutputStream(outputFile);
			IOUtils.copy(stream, output);
		} catch (IOException e) {
			throw new UtilUncheckedException(
					"Could not copy contents out to file " + outputFile, e);
		} finally {
			closeQuietly(output);
		}
	}

	/**
	 * Attempts to read a list of strings from the resource, which is first
	 * checked as a file and then as a classpath resource
	 * 
	 * @param resource
	 *            filename or classpath resource
	 * @return list of strings read from resource
	 */
	public static List<String> resourceToList(String resource) {
		List<String> l = CollectionUtils.createArrayList();
		try {
			File res = new File(resource);
			if (!res.exists()) {
				res = null;
				if (resourceExists(resource)) {
					res = File.createTempFile("resource", ".tmp");
					InputOutputUtils.copyFromClasspathToFileSystem(resource,
							res);
				}
			}
			if (res != null)
				l.addAll(org.apache.commons.io.FileUtils.readLines(res, null));
		} catch (IOException e) {
			throw new UtilUncheckedException("Could not read from resource "
					+ resource, e);
		}
		return l;
	}

	public static Map<String, String> resourceToMap(String resource) {
		Map<String, String> map = CollectionUtils.createHashMap();
		for (String line : resourceToList(resource)) {
			String[] cols = line.split("\\s+");
			if (cols.length == 2) {
				map.put(cols[0], cols[1]);
			}
		}
		return map;
	}

	/**
	 * @param resource
	 *            classpath resource
	 * @return true if resource exists and can be read
	 */
	public static boolean resourceExists(String resource) {
		return InputOutputUtils.class.getResourceAsStream(resource) != null;
	}

}
