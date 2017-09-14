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
 * File: ChecksumDelegator.java
 * Created by: mhaimel
 * Created on: 11 Dec 2007
 * CVS: $Id$
 */
package uk.ac.ebi.proteome.util.checksums;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.zip.Checksum;

import uk.ac.ebi.proteome.util.InputOutputUtils;
import uk.ac.ebi.proteome.util.UtilUncheckedException;

/**
 * @author mhaimel
 * @author $Author$
 * @version $Revision$
 */
public class ChecksumDelegator implements StreamingChecksum {
	
	private Checksum checksum;

	/**
	 * @param checksum
	 */
	public ChecksumDelegator(Checksum checksum) {
		super();
		this.checksum = checksum;
	}

	/**
	 * @return
	 * @see java.util.zip.Checksum#getValue()
	 */
	public long getValue() {
		return checksum.getValue();
	}

	/**
	 * 
	 * @see java.util.zip.Checksum#reset()
	 */
	public void reset() {
		checksum.reset();
	}

	/**
	 * @param b
	 * @param off
	 * @param len
	 * @see java.util.zip.Checksum#update(byte[], int, int)
	 */
	public void update(byte[] b, int off, int len) {
		checksum.update(b, off, len);
	}

	/**
	 * @param b
	 * @see java.util.zip.Checksum#update(int)
	 */
	public void update(int b) {
		checksum.update(b);
	}

	public void process(InputStream is) {
		int val = -1;
		try {
			while((val = is.read()) != -1){
				this.update(val);
			}
		} catch (IOException e) {
			throw new UtilUncheckedException("Could not read next value " +
					"from input stream", e);
		}
	}

	public void process(String string) {
		for(int i = 0; i < string.length(); ++i) {
			this.update(string.charAt(i));
		}		
	}

	public void process(File file) {
		Reader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			process(reader);
		}
		catch (FileNotFoundException e) {
			throw new UtilUncheckedException("Cannot open file "+file, e);
		}
		finally {
			InputOutputUtils.closeQuietly(reader);
		}		
	}

	public void process(Reader reader) {
		int val = -1;
		try {
			while((val = reader.read()) != -1){
				this.update(val);
			}
		} catch (IOException e) {
			throw new UtilUncheckedException("Could not read next value " +
					"from input stream", e);
		}		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return this.checksum.toString();
	}	
	
}
