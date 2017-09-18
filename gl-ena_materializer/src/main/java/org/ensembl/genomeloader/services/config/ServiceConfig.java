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
 * File: ServiceConfig.java
 * Created by: dstaines
 * Created on: Nov 10, 2006
 * CVS:  $Id$
 */
package org.ensembl.genomeloader.services.config;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.ensembl.genomeloader.services.ServiceUncheckedException;

/**
 * A bean containing all properties needed. getInstance() will initialise a
 * class singleton
 *
 * @author dstaines
 *
 */
public class ServiceConfig {

	/**
	 * Output location for generated Java classes
	 */
	private String apiSrcLocation;

	/**
	 * Time in milliseconds that a remote connection is permitted to idle before
	 * being closed
	 */
	private int connectionIdleTime = 30000;

	/**
	 * Follow a symbolic link in a remote connection
	 */
	private boolean followLink = true;

	/**
	 * Use passive mode for a remote FTP connection
	 */
	private boolean usePassiveMode = false;

	/**
	 * Directory for storage of working data and log files
	 */
	private String dataDirectory;

	/**
	 * Directory for creation of file-based locks
	 */
	private String lockDirectory;
	
	/**
	 * Time in milliseconds that a JDBC connection is permitted to idle before
	 * being closed
	 */
	private int dbIdleTime = 30000;

	/**
	 * Default role to grant for tables created with TableControl
	 */
	private String defaultGrantRole;

	/**
	 * Address for any mail generated, and also the password for anonymous FTP
	 * access
	 */
	private String emailAddress;

	/**
	 * Field delimiter string for data files to be uploaded
	 */
	private String fieldDelimiter = "^^";

	/**
	 * Path to general services log file
	 */
	private String logFile;

	/**
	 * Maximum number of pooled connections to a given JDBC URI
	 */
	private int maxDbConnections = 3;

	/**
	 * Maximum number of pooled JDBC connections
	 */
	private int maxDbConnectionsTotal = 15;

	/**
	 * Maximum number of times to retry a job with transient failures
	 */
	private int maxJobRetries = 3;

	/**
	 * Maximum number of times to retry a job with transient failures
	 */
	private int jobRetrySleep = 30000;

	/**
	 * Maximum number of jobs to execute in parallel
	 */
	private int maxJobs = 5;

	/**
	 * Maximum number of remote connections to a given URI to hold in pool
	 */
	private int maxRemoteConnections = 3;

	/**
	 * Maximum number of any remote connections to hold in pool
	 */
	private int maxRemoteConnectionsTotal = 15;

	/**
	 * Maximum number of JDBC prepared statements to cache for each pooled
	 * connection
	 */
	private int maxStatementsTotal = 25;

	/**
	 * Name of Oracle directory to use for loading data into the mirror
	 */
	private String mirrorDbDirectory;

	/**
	 * URI identifying location of Oracle directory to use for loading data into
	 * the mirror
	 */
	private String mirrorDbDirectoryURI;

	/**
	 * Index space to use for the mirror database
	 */
	private String mirrorDbIndexspace;

	/**
	 * Table space to use for the mirror database
	 */
	private String mirrorDbTablespace;

	/**
	 * JDBC URI for the mirror database
	 */
	private String mirrorDbUri;

	/**
	 * String to use as delimiter for records to load
	 */
	private String recordDelimiter = "<end>\n";

	/**
	 * Command to use for transferring files using sbftp (script-based file
	 * transfer protocol)
	 */
	private String sbftpCommand;

	/**
	 * JDBC URI to use for database backing services
	 */
	private String serviceDbUri;

	/**
	 * HTTP URL for services server
	 */
	private String serviceUrl;

	/**
	 * Path to XML file containing data source definitions
	 */
	private String sourceFile;

	/**
	 * Length of time a statement should remain idle before being evicted
	 */
	private int statementIdleTime = 30000;

	/**
	 * Command to use for invoking SQL loader
	 */
	private String sqlLoaderCommand = "./bin/runSqlLoader.sh {0} {1} {2}";

	/**
	 * @deprecated
	 */
	private boolean useCheckpoints = false;

	/**
	 * Number of load operations to carry out in parallel for a given datasource
	 */
	private int parallelDataLoaderN = 5;

	public ServiceConfig() {
	}

	/**
	 * Used as a method of cloning using an existing configuration object. To
	 * reduce complexity of the code this attempts to perform the function by
	 * reflection using {@link BeanUtils#copyProperties(Object, Object)}
	 */
	public ServiceConfig(ServiceConfig config) {
		try {
			BeanUtils.copyProperties(this, config);
		}
		catch (IllegalAccessException e) {
			throw new ServiceUncheckedException("Cannot create " +
					"new instance of ServiceConfig from previous version", e);
		}
		catch (InvocationTargetException e) {
			throw new ServiceUncheckedException("Cannot create " +
					"new instance of ServiceConfig from previous version", e);
		}
	}

	public String getApiSrcLocation() {
		return this.apiSrcLocation;
	}

	public int getConnectionIdleTime() {
		return this.connectionIdleTime;
	}

	public String getDataDirectory() {
		return this.dataDirectory;
	}

	public int getDbIdleTime() {
		return this.dbIdleTime;
	}

	public String getDefaultGrantRole() {
		return this.defaultGrantRole;
	}

	public String getEmailAddress() {
		return this.emailAddress;
	}

	public String getFieldDelimiter() {
		return this.fieldDelimiter;
	}

	public String getLogFile() {
		return this.logFile;
	}

	public int getMaxDbConnections() {
		return this.maxDbConnections;
	}

	public int getMaxDbConnectionsTotal() {
		return this.maxDbConnectionsTotal;
	}

	public int getMaxJobRetries() {
		return this.maxJobRetries;
	}

	public int getMaxJobs() {
		return this.maxJobs;
	}

	public int getMaxRemoteConnections() {
		return this.maxRemoteConnections;
	}

	public int getMaxRemoteConnectionsTotal() {
		return this.maxRemoteConnectionsTotal;
	}

	public int getMaxStatementsTotal() {
		return this.maxStatementsTotal;
	}

	public String getMirrorDbDirectory() {
		return mirrorDbDirectory;
	}

	public String getMirrorDbIndexspace() {
		return mirrorDbIndexspace;
	}

	public String getMirrorDbTablespace() {
		return mirrorDbTablespace;
	}

	public String getMirrorDbUri() {
		return this.mirrorDbUri;
	}

	public String getRecordDelimiter() {
		return this.recordDelimiter;
	}

	public String getServiceDbUri() {
		return this.serviceDbUri;
	}

	public String getServiceUrl() {
		return this.serviceUrl;
	}

	public String getSourceFile() {
		return this.sourceFile;
	}

	public int getStatementIdleTime() {
		return this.statementIdleTime;
	}

	public boolean isUseCheckpoints() {
		return this.useCheckpoints;
	}

	public void setApiSrcLocation(String apiSrcLocation) {
		this.apiSrcLocation = apiSrcLocation;
	}

	public void setConnectionIdleTime(int connectionIdleTime) {
		this.connectionIdleTime = connectionIdleTime;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public void setDbIdleTime(int dbIdleTime) {
		this.dbIdleTime = dbIdleTime;
	}

	public void setDefaultGrantRole(String defaultGrantRole) {
		this.defaultGrantRole = defaultGrantRole;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public void setFieldDelimiter(String fieldDelimiter) {
		this.fieldDelimiter = fieldDelimiter;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public void setMaxDbConnections(int maxDbConnections) {
		this.maxDbConnections = maxDbConnections;
	}

	public void setMaxDbConnectionsTotal(int maxDbConnectionsTotal) {
		this.maxDbConnectionsTotal = maxDbConnectionsTotal;
	}

	public void setMaxJobRetries(int maxJobRetries) {
		this.maxJobRetries = maxJobRetries;
	}

	public void setMaxJobs(int maxJobs) {
		this.maxJobs = maxJobs;
	}

	public void setMaxRemoteConnections(int maxRemoteConnections) {
		this.maxRemoteConnections = maxRemoteConnections;
	}

	public void setMaxRemoteConnectionsTotal(int maxRemoteConnectionsTotal) {
		this.maxRemoteConnectionsTotal = maxRemoteConnectionsTotal;
	}

	public void setMaxStatementsTotal(int maxStatementsTotal) {
		this.maxStatementsTotal = maxStatementsTotal;
	}

	public void setMirrorDbDirectory(String mirrorDbDirectory) {
		this.mirrorDbDirectory = mirrorDbDirectory;
	}

	public void setMirrorDbIndexspace(String mirrorDbIndexspace) {
		this.mirrorDbIndexspace = mirrorDbIndexspace;
	}

	public void setMirrorDbTablespace(String mirrorDbTablespace) {
		this.mirrorDbTablespace = mirrorDbTablespace;
	}

	public void setMirrorDbUri(String mirrorDbUri) {
		this.mirrorDbUri = mirrorDbUri;
	}

	public void setRecordDelimiter(String recordDelimiter) {
		this.recordDelimiter = recordDelimiter;
	}

	public void setServiceDbUri(String servicesDbUri) {
		this.serviceDbUri = servicesDbUri;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}

	public void setStatementIdleTime(int statementIdleTime) {
		this.statementIdleTime = statementIdleTime;
	}

	public void setUseCheckpoints(boolean useCheckpoints) {
		this.useCheckpoints = useCheckpoints;
	}

	public String getMirrorDbDirectoryURI() {
		return this.mirrorDbDirectoryURI;
	}

	public String getSbftpCommand() {
		return this.sbftpCommand;
	}

	public void setMirrorDbDirectoryURI(String mirrorDbDirectoryURI) {
		this.mirrorDbDirectoryURI = mirrorDbDirectoryURI;
	}

	public void setSbftpCommand(String sbftpCommand) {
		this.sbftpCommand = sbftpCommand;
	}

	public String getSqlLoaderCommand() {
		return this.sqlLoaderCommand;
	}

	public void setSqlLoaderCommand(String sqlLoaderCommand) {
		this.sqlLoaderCommand = sqlLoaderCommand;
	}

	public int getParallelDataLoaderN() {
		return parallelDataLoaderN;
	}

	public void setParallelDataLoaderN(int parallelDataLoaderN) {
		this.parallelDataLoaderN = parallelDataLoaderN;
	}

	public boolean isFollowLink() {
		return followLink;
	}

	public void setFollowLink(boolean followLink) {
		this.followLink = followLink;
	}

	public boolean isUsePassiveMode() {
		return usePassiveMode;
	}

	public void setUsePassiveMode(boolean usePassiveMode) {
		this.usePassiveMode = usePassiveMode;
	}

	/**
	 * @return the jobRetrySleep
	 */
	public int getJobRetrySleep() {
		return jobRetrySleep;
	}

	/**
	 * @param jobRetrySleep the jobRetrySleep to set
	 */
	public void setJobRetrySleep(int jobRetrySleep) {
		this.jobRetrySleep = jobRetrySleep;
	}
	
	public String getLockDirectory() {
		return lockDirectory;
	}

	public void setLockDirectory(String lockDirectory) {
		this.lockDirectory = lockDirectory;
	}

	public String toString() {
		return ReflectionToStringBuilder.toString(this);
	}
}
