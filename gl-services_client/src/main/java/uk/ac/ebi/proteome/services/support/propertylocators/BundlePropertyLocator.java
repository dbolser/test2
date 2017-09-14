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

package uk.ac.ebi.proteome.services.support.propertylocators;

import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringUtils;

/**
 * Backs onto a given {@link ResourceBundle}. This version of the locator does
 * not support {@link #setProperty(String, String)} and will throw an
 * {@link UnsupportedOperationException} if asked to do so
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class BundlePropertyLocator extends AbstractPropertyLocator<String, String> {

	private ResourceBundle resourceBundle = null;
	private final String resourceBundleLocation;

	public BundlePropertyLocator(String bundleLocation) {
		this.resourceBundleLocation = bundleLocation;
	}

	/**
	 * Returns a value from the bundle. Will be null if the resource cannot be
	 * found
	 */
	public String getPropertyFromBundle(String key) {
		String value = null;
		ResourceBundle bundle = getBundle();
		if (bundle != null) {
			try {
				value = bundle.getString(key);
			} catch (MissingResourceException e) {
				// Cannot find resource - ignore
			} catch (ClassCastException e) {
				// resource was not a string - ignore
			}
		}
		return value;
	}

	/**
	 * Returns the bundle for {@link #resourceBundleLocation}
	 */
	public ResourceBundle getBundle() {
		if (resourceBundle == null) {
			try {
				resourceBundle = PropertyResourceBundle
					.getBundle(resourceBundleLocation);
			}
			catch(Exception e) {
				// Cannot find resource ... will be quiet because otherwise we
				// get problems with the logger and startup order
			}
		}
		return resourceBundle;
	}

	@Override
	protected boolean isEmpty(String value) {
		return StringUtils.isEmpty(value);
	}

	/**
	 * Throws {@link UnsupportedOperationException} since {@link ResourceBundle}
	 * do not support this method of setting a property
	 */
	public void setProperty(String key, String value) {
		throw new UnsupportedOperationException("Cannot perform a set on "
				+ "this property locator");
	}

	@Override
	protected String provideProperty(String key) {
		return getPropertyFromBundle(key);
	}
}
