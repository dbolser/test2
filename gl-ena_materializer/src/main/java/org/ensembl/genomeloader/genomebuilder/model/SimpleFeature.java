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

package org.ensembl.genomeloader.genomebuilder.model;

import java.util.List;
import java.util.Map;

/**
 * Interface for anything that doesn't fit into the other genomic features
 * model. Intentionally generic, and designed to accept ENA features for further
 * manipulation into Ensembl features
 */
public interface SimpleFeature extends Locatable, CrossReferenced, Identifiable, Integr8ModelComponent {

	/**
	 * @return type of feature e.g. misc_difference
	 */
	public String getFeatureType();

	/**
	 * @return description of feature
	 */
	public String getDisplayLabel();
	
	/**
	 * @return map of semi-structured information
	 */
	public Map<String, List<String>>  getQualifiers();
	
}
