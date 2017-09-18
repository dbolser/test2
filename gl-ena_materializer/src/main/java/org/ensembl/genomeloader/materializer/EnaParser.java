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
 * File: EnaComponentMaterializer.java
 * Created by: dstaines
 * Created on: Mar 23, 2010
 * CVS:  $$
 */
package org.ensembl.genomeloader.materializer;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

/**
 * Interface for parsing an ENA record into a generic object
 * 
 * @author dstaines
 * 
 */
public interface EnaParser<T> {

	public abstract T parse(URL url);

	public abstract T parse(File file);

	public abstract T parse(InputStream record);

}
