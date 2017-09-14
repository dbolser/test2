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
 * File: URIConnectionReport.java
 * Created by: dstaines
 * Created on: Nov 29, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dstaines
 * 
 */
public class URIConnectionInfo {

	private static Pattern pat = Pattern.compile("jdbc:oracle:thin:([^/]+)/.*");

	public URIConnectionInfo(String uri) {
		this.uri = uri;
		Matcher m = pat.matcher(uri);
		if (m.matches()) {
			user = m.group(1);
		} else {
			user = "anonymous";
		}
	}

	private String user;

	private String uri;

	private int idleConnections;

	private int activeConnections;

	private int userCursors;

	private List<SqlStatementInfo> statementInfoList = new ArrayList<SqlStatementInfo>();

	private Map<String, CursorUsage> cursorUseSummary = new HashMap<String, CursorUsage>();

	public Map<String, CursorUsage> getCursorUseSummary() {
		return cursorUseSummary;
	}

	private List<ConnectionInfo> connections = new ArrayList<ConnectionInfo>();

	public int getTotalCursors() {
		int t = 0;
		for (CursorUsage c : cursorUseSummary.values()) {
			t += c.getCursors();
		}
		return t;
	}

	public int getUserCursors() {
		int t = 0;
		for (CursorUsage c : cursorUseSummary.values()) {
			if (user.equalsIgnoreCase(c.getUser())) {
				t += c.getCursors();
			}
		}
		return t;
	}

	public List<ConnectionInfo> getConnections() {
		return this.connections;
	}

	public String getUri() {
		return this.uri;
	}

	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append("URI:\t" + uri + "\n");
		b.append("Active:\t" + activeConnections + "\n");
		b.append("Idle:\t" + idleConnections + "\n");
		b.append(user + " c:\t" + userCursors + "\n");
		b.append("Total c:\t" + getTotalCursors() + "\n");
		return b.toString();
	}

	public String toReport() {
		StringBuffer b = new StringBuffer();
		b.append(this.toString());
		b.append("Cursor summary:\n");
		for (CursorUsage c : getCursorUseSummary().values()) {
			b.append(c+"\n");
		}
		b.append("SQL summary:\n");
		for(SqlStatementInfo info: statementInfoList) {
			b.append(info+"\n");			
		}
//		for (Entry es : getStatementInfoList().entrySet()) {
//			b.append("\"" + es.getKey() + "\"\t" + es.getValue()+"\n");
//		}
		return b.toString();
	}

	public void addConnection(ConnectionInfo con) {
		connections.add(con);
	}

	public void addStatementInfo(SqlStatementInfo info) {
		statementInfoList.add(info);
	}

	public void addCursorUsage(CursorUsage usage) {
		cursorUseSummary.put(usage.getSid(), usage);
	}

	public int getActiveConnections() {
		return this.activeConnections;
	}

	public void setActiveConnections(int activeConnections) {
		this.activeConnections = activeConnections;
	}

	public int getIdleConnections() {
		return this.idleConnections;
	}

	public void setIdleConnections(int idleConnections) {
		this.idleConnections = idleConnections;
	}

	public List<SqlStatementInfo> getStatementInfoList() {
		return this.statementInfoList;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
