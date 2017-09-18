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

package uk.ac.ebi.proteome.genomebuilder.model.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.ac.ebi.proteome.genomebuilder.model.DatabaseReference;
import uk.ac.ebi.proteome.genomebuilder.model.EntityLocation;
import uk.ac.ebi.proteome.genomebuilder.model.SimpleFeature;
import uk.ac.ebi.proteome.util.collections.CollectionUtils;

/**
 * Created by IntelliJ IDEA. User: arnaud Date: 11-Jul-2008 Time: 17:06:56
 */
public class SimpleFeatureImpl implements SimpleFeature {

	private static final long serialVersionUID = 1L;
	private EntityLocation location;
	private String featureType;
	private String displayLabel;
	private Map<String, List<String>> qualifiers;
	private Set<DatabaseReference> refs;
	private String identifyingId;
	private String idString;

	public EntityLocation getLocation() {
		return location;
	}

	public void setLocation(EntityLocation location) {
		this.location = location;
	}

	public String getFeatureType() {
		return featureType;
	}

	public void setFeatureType(String analysis) {
		this.featureType = analysis;
	}

	public String getDisplayLabel() {
		return displayLabel;
	}

	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	public Map<String, List<String>> getQualifiers() {
		if (qualifiers == null)
			qualifiers = CollectionUtils.createHashMap();
		return qualifiers;
	}

	public void addQualifier(String key, String value) {
		List<String> vals = getQualifiers().get(key);
		if (vals == null) {
			vals = CollectionUtils.createArrayList();
			addQualifier(key, vals);
		}
		vals.add(value);
	}

	public void addQualifier(String key, List<String> value) {
		getQualifiers().put(key, value);
	}

	public Set<DatabaseReference> getDatabaseReferences() {
		if (refs == null)
			refs = CollectionUtils.createHashSet();
		return refs;
	}

	public void addDatabaseReference(DatabaseReference reference) {
		getDatabaseReferences().add(reference);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.genomebuilder.model.Identifiable#getIdentifyingId()
	 */
	public String getIdentifyingId() {
		return identifyingId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see uk.ac.ebi.proteome.genomebuilder.model.Identifiable#setIdentifyingId(java.lang.String)
	 */
	public void setIdentifyingId(String identifyingId) {
		this.identifyingId = identifyingId;
	}

	public String getIdString() {
		return this.idString;
	}
	
	public void setIdString(String id) {
		this.idString = id;
	}	
	
}
