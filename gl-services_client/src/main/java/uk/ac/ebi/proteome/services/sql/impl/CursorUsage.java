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
 * File: CursorUsage.java
 * Created by: dstaines
 * Created on: Nov 29, 2006
 * CVS:  $Id$
 */
package uk.ac.ebi.proteome.services.sql.impl;

/**
 * @author dstaines
 *
 */
public class CursorUsage {
	private int cursors;
	private String sid;
	private String user;
	private String host;
	private String program;
	public int getCursors() {
		return this.cursors;
	}
	public void setCursors(int cursors) {
		this.cursors = cursors;
	}
	public String getHost() {
		return this.host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getProgram() {
		return this.program;
	}
	public void setProgram(String program) {
		this.program = program;
	}
	public String getSid() {
		return this.sid;
	}
	public void setSid(String sid) {
		this.sid = sid;
	}
	public String getUser() {
		return this.user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	/**
	 * @param cursors
	 * @param sid
	 * @param user
	 * @param host
	 * @param program
	 */
	public CursorUsage(int cursors, String sid, String user, String host, String program) {
		super();
		this.cursors = cursors;
		this.sid = sid;
		this.user = user;
		this.host = host;
		this.program = program;
	}
	
	public String toString() {
		return sid + "\t" + user + "\t" + host + "\t" + program + "\t" + cursors;
	}
}
