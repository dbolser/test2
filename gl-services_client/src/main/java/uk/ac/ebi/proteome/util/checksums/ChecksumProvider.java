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
 * File: ChecksumProvider.java
 * Created by: mhaimel
 * Created on: 11 Dec 2007
 * CVS: $Id$
 */
package uk.ac.ebi.proteome.util.checksums;

import java.util.zip.CRC32;

import org.biojavax.utils.CRC64Checksum;

/**
 * @author mhaimel
 * @author $Author$
 * @version $Revision$
 */
public class ChecksumProvider {
	
	public static StreamingChecksum getCrc64(){
		return new ChecksumDelegator(new CRC64Checksum());
	}
	
//	public static String getCrc64(String input){
//		StreamingChecksum processor = getCrc64();
//		processor.process(input);
//		return processor.toString();
//	}
	
	public static StreamingChecksum getCrc32(){
		return new ChecksumDelegator(new CRC32());
	}	

}
