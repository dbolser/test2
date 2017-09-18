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

package org.ensembl.genomeloader.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains the listing of all drivers that the services are aware of and
 * can interact with via the SQL Service.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public enum DatabaseSettings {

	ORACLE("jdbc:oracle:thin.*", "oracle.jdbc.driver.OracleDriver"),

	MYSQL("jdbc:mysql.*", "com.mysql.jdbc.Driver"),

	HSQLDB("jdbc:hsqldb.*", "org.hsqldb.jdbcDriver"),

	POSTGRESSQL("jdbc:postgresql.*", "org.postgresql.Driver");

	private DatabaseSettings(String urlRegex, String driver) {
		this.urlRegex = Pattern.compile(urlRegex);
		this.driver = driver;
	}

	private final Pattern urlRegex;

	private final String driver;

	public Pattern getUrlRegex() {
		return urlRegex;
	}

	public String getDriver() {
		return driver;
	}

	/**
	 * Loops through all known patterns & attempts to look for a URL which will
	 * cause a successful match using {@link Matcher#matches()}.
	 * If none can be found this method will return null.
	 */
	public static DatabaseSettings getSettingsForUri(String url) {
		DatabaseSettings settings = null;
		for(DatabaseSettings current: values()) {
			Matcher matcher = current.getUrlRegex().matcher(url);
			if(matcher.matches()) {
				settings = current;
				break;
			}
		}
		return settings;
	}

	/**
	 * Returns all JDBC drivers currently known to this class
	 */
	public static String[] getDrivers() {
		DatabaseSettings[] settings = values();
		String[] drivers = new String[settings.length];
		for(int i=0; i<settings.length; i++) {
			drivers[i] = settings[i].getDriver();
		}
		return drivers;
	}
}
