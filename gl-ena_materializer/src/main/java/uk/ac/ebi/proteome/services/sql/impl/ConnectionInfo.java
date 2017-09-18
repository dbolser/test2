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
 * File: ConnectionInfo.java
 * Created by: dstaines
 * Created on: Nov 29, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql.impl;

import java.util.ArrayList;
import java.util.List;

import uk.ac.ebi.proteome.services.sql.DatabaseConnection;
import uk.ac.ebi.proteome.services.sql.ROResultSet;

/**
 * Wrapper class for information about a connection
 * @author dstaines
 *
 */
public class ConnectionInfo {

	private DatabaseConnection con;

	public ConnectionInfo(DatabaseConnection con, boolean isActive) {
		this.con = con;
		this.active = isActive;
	}

	private boolean active = false;

	public boolean isActive() {
		return this.active;
	}

	public List<String> getCalls() {
		return new ArrayList<String>(con.getCalls().keySet());
	}

	public List<String> getStatements() {
		return new ArrayList<String>(con.getPreparedStatements().keySet());
	}

	public ROResultSet getResultSet() {
		return con.getResultSet();
	}

	public String getUrl() {
		return con.getUrl();
	}

	public int getId() {
		return con.getId();
	}
}
