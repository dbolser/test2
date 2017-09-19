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
 * File: XmlEnaComponentParser.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ensembl.genomeloader.materializer.EnaParsingException;
import org.ensembl.genomeloader.materializer.executor.SimpleExecutor;
import org.ensembl.genomeloader.util.InputOutputUtils;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

/**
 * @author dstaines
 * 
 */
public abstract class AbstractXmlEnaParser<T> {

	private static final int MAX_TRIES = 3;
	private static final long SLEEP_TIME = 30000;

	private Log log;

	protected Log getLog() {
		if (log == null) {
			log = LogFactory.getLog(this.getClass());
		}
		return log;
	}

	private final Executor executor;

	public AbstractXmlEnaParser() {
		this(new SimpleExecutor());
	}

	public AbstractXmlEnaParser(Executor executor) {
		this.executor = executor;
	}

	public T parse(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			return parse(is);
		} catch (FileNotFoundException e) {
			throw new EnaParsingException(
					"Could not parse ENA record from file " + file.getPath(), e);
		} finally {
			InputOutputUtils.closeQuietly(is);
		}
	}

	public T parse(InputStream record) {
		return parse(parseDocument(record));
	}

	public abstract T parse(Document doc);

	public T parse(final URL url) {
		try {
			final File f = File.createTempFile("ENA", ".xml");
			executor.execute(new Runnable() {
				public void run() {
					int tries = 0;
					InputStream is = null;
					while (tries < MAX_TRIES) {
						try {
							URLConnection uc = url.openConnection();
							is = uc.getInputStream();
							InputOutputUtils.copyInputStreamToFileSystem(is, f);
							break;
						} catch (IOException e) {
							if (tries++ < MAX_TRIES) {
								getLog().warn(
										"Could not parse ENA record from URL "
												+ url + ": retrying", e);
								try {
									Thread.sleep(SLEEP_TIME);
								} catch (InterruptedException e1) {
									getLog().warn("Woke up from sleep", e1);
								}
							} else {
								throw new EnaParsingException(
										"Could not parse ENA record from URL "
												+ url, e);
							}
						} finally {
							InputOutputUtils.closeQuietly(is);
						}
					}
				}
			});
			if (f == null) {
				throw new EnaParsingException(
						"Could not parse ENA record from URL " + url);
			}
			try {
				return parse(f);
			} finally {
				f.delete();
			}
		} catch (IOException e) {
			throw new EnaParsingException(e);
		}

	}

	public Document parseDocument(InputStream record) {
		try {
			Document doc = new Builder().build(new BufferedReader(
					new InputStreamReader(record)));
			return doc;
		} catch (ValidityException e) {
			throw new EnaParsingException("Could not parse ENA record", e);
		} catch (ParsingException e) {
			throw new EnaParsingException("Could not parse ENA record", e);
		} catch (IOException e) {
			throw new EnaParsingException("Could not parse ENA record", e);
		} finally {
		}
	}

}
