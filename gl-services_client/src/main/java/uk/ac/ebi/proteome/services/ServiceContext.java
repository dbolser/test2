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

package uk.ac.ebi.proteome.services;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import uk.ac.ebi.proteome.services.config.ServiceConfig;
import uk.ac.ebi.proteome.services.sql.SqlService;
import uk.ac.ebi.proteome.services.support.DefaultServiceConfigProvider;
import uk.ac.ebi.proteome.services.support.DefaultServiceProvider;
import uk.ac.ebi.proteome.services.support.ServicesEnum;
import uk.ac.ebi.proteome.services.support.UpdateProcessOwnerAccessLockIdProvider;
import uk.ac.ebi.proteome.services.support.propertylocators.DefaultPropertyLocator;
import uk.ac.ebi.proteome.services.support.propertylocators.DelegatingPropertyLocator;
import uk.ac.ebi.proteome.services.support.propertylocators.MapPropertyLocator;
import uk.ac.ebi.proteome.util.reflection.ReflectionUtils;

/**
 * New implementation of the service context concept which now rationalises
 * the design on two levels:
 *
 * <ul>
 * <li>All access is through the {@link #getInstance()} method after
 * which all access is via public methods</li>
 * <li>Service provision is from {@link ServiceProvider} instances</li>
 * </ul>
 *
 * This means that ServiceContext becomes a way of locating contextual
 * information and services in a runtime environment.
 *
 * <p>
 * As a by product of this change this context is now no longer a true
 * singleton. We allow public access to the no-arguments constructor which
 * allows a programmer to construct their own context which is very useful
 * for test environments.
 *
 * @author ayates
 * @author $Author$
 * @version $Revision$
 */
public class ServiceContext {

	/**
	 * Used to hold the instances of properties used in the ServiceContext
	 * and can be accessed throughout the context using systems
	 *
	 * @author ayates
	 */
	public enum Properties {
		CONFIG_FILE("configFile"),
		PROCESS_NAME("processName"),
		UPDATE_PROCESS_NAME("updateProcessName"),
		SUBMISSION_COMMAND("submissionCommand"),
		SUBMISSION_COMMAND_ARGS("submissionCommandArgs"),
		STATUS_COMMAND("statusCommand"),
		KILL_COMMAND("killCommand"),
		EXECUTION_COMMAND("executionCommand"),
		EXECUTION_COMMAND_ARGS("executionCommandArgs"),
		CONFIG_FACTORY("configFactory"),
		EXECUTOR_WAIT_TIME("executorWaitTime"),
		CONTROLLER_WAIT_TIME("controllerWaitTime");

		private final String property;

		private Properties(String property) {
			this.property = property;
		}

		public String getProperty() {
			return property;
		}
	}

	/**
	 * Initalise on demand idiom singleton holder class
	 *
	 * @author ayates
	 */
	private static class SingletonHolder {
		public static ServiceContext CONTEXT = null;

		/* Post construction modification of the context in a static block
		 */
		static {
			CONTEXT = new ServiceContext();
			CONTEXT.setServiceConfigProvider(new DefaultServiceConfigProvider(CONTEXT));
			CONTEXT.setPropertyLocator(new DefaultPropertyLocator());
			CONTEXT.setServiceProvider(new DefaultServiceProvider(CONTEXT));
			CONTEXT.setAccessLockIdProvider(new UpdateProcessOwnerAccessLockIdProvider(CONTEXT));
		}
	}

	/**
	 * Thankfully a class with more memory than Lenny from Memento. This
	 * object will record the current Objects being used in the context
	 * used to construct it up with. When using this object you must be very
	 * careful of what is modified after a memento is created. The objects where
	 * a deep copy is not applied are:
	 *
	 * <ul>
	 * <li> {@link ServiceProvider} </li>
	 * <li> {@link PropertyLocator} - If you are going to override properties
	 * then try wrapping your replacing locator in a
	 * {@link DelegatingPropertyLocator} mapping into a
	 * {@link MapPropertyLocator}</li>
	 * <li> {@link PipelineJob} from {@link ServiceContext#getCurrentJob()} </li>
	 * <li> {@link ServiceConfigProvider} </li>
	 * </ul>
	 *
	 * All other objects are copied correctly. Note that the thread local
	 * variables are not stored in their own new thread local variables. The
	 * object assumes that you will be restoring from the same context you
	 * created the memento in.
	 *
	 * <p>
	 * When restoring the context state we do a re-assignment of all stored
	 * objects.
	 *
	 * @author ayates
	 * @author $Author$
	 * @version $Revision$
	 */
	public static class ContextMemento {

		private ServiceProvider serviceProvider;
		private PropertyLocator<String, String> propertyLocator;
		private ServiceConfigProvider serviceConfigProvider;
		private AccessLockIdProvider accessLockIdProvider;
		private ServiceConfig config;
		private String processName;
		private String updateProcessName;
		private String accesssLockId;

		public ContextMemento(ServiceContext context) {
			//Copy by ref
			serviceProvider = context.getServiceProvider();
			propertyLocator = context.getPropertyLocator();
			serviceConfigProvider = context.getServiceConfigProvider();
			accessLockIdProvider = context.getAccessLockIdProvider();

			//Immutable copies (thank you Strings)
			processName = context.getProcessName();
			updateProcessName = context.getUpdateProcessName();
			if(null != accessLockIdProvider){
				accesssLockId = context.getAccessLockId();
			}

			//Config copy
			if(null != serviceConfigProvider && context.getConfig() != null) {
				config = new ServiceConfig(context.getConfig());
			}
		}

		/**
		 * Used to set a context to the state this memento was created under.
		 *
		 * @param context The context to restore to this current memento's
		 * state
		 */
		public void restoreContext(ServiceContext context) {
			context.setServiceProvider(serviceProvider);
			context.setPropertyLocator(propertyLocator);
			context.setServiceConfigProvider(serviceConfigProvider);
			context.setAccessLockIdProvider(accessLockIdProvider);

			context.setConfig(config);
			context.setProcessName(processName);
			context.setUpdateProcessName(updateProcessName);
			if(null != accessLockIdProvider){
				context.setAccessLockId(accesssLockId);
			}
		}
	}

	/**
	 * Explicitly allowing zero argument construction
	 */
	public ServiceContext() {
		super();
	}

	/**
	 * Allows for construction of a context from pre-constructed objects.
	 */
	public ServiceContext(PropertyLocator<String, String> propertyLocator,
			ServiceConfig serviceConfig, ServiceProvider serviceProvider) {
		setPropertyLocator(propertyLocator);
		setConfig(serviceConfig);
		setServiceProvider(serviceProvider);
	}

	/**
	 * Returns the current singleton instance of the {@link ServiceContext}
	 */
	public static ServiceContext getInstance() {
		return SingletonHolder.CONTEXT;
	}

	private ServiceProvider serviceProvider = null;
	private ServiceConfig config = null;
	private PropertyLocator<String, String> propertyLocator = null;
	private ServiceConfigProvider serviceConfigProvider = null;
	private AccessLockIdProvider accessLockIdProvider = null;

	private String baseDirectory = null;

	private String configFile = null;

	private InheritableThreadLocal<String> processName =
		new InheritableThreadLocal<String>();

	private InheritableThreadLocal<String> updateProcessName =
		new InheritableThreadLocal<String>();

	private InheritableThreadLocal<String> accessLockId =
		new InheritableThreadLocal<String>();

	/**
	 * Returns the current service provider
	 */
	public ServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	/**
	 * Allows you to set the current service provider. Normally this will have
	 * been done for you.
	 */
	public void setServiceProvider(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	/**
	 * Returns an instance of the current service configuration. If
	 * {@link #config} is set to null then we request a new one from
	 * {@link #getServiceConfigProvider()} and its method
	 * {@link ServiceConfigProvider#getServiceConfig()}
	 */
	public ServiceConfig getConfig() {
		if(config == null) {
			config = getServiceConfigProvider().getServiceConfig();
		}
		return config;
	}

	/**
	 * Allows you to set the current service configuration globally. Setting this
	 * to null will cause the configuration to be re-requested (see
	 * {@link #getConfig()} for more information on this procedure)
	 */
	public void setConfig(ServiceConfig config) {
		this.config = config;
	}

	/**
	 * Returns the current property locator
	 */
	public PropertyLocator<String, String> getPropertyLocator() {
		return propertyLocator;
	}

	/**
	 * Allows you to replace the current property locator object
	 */
	public void setPropertyLocator(PropertyLocator<String, String> propertyLocator) {
		this.propertyLocator = propertyLocator;
	}

	/**
	 * Used to return the current {@link ServiceConfig} provider object. Normally
	 * is run when {@link #config} is set to null
	 */
	public ServiceConfigProvider getServiceConfigProvider() {
		return serviceConfigProvider;
	}

	/**
	 * Sets the current configuration provider
	 */
	public void setServiceConfigProvider(
			ServiceConfigProvider serviceConfigProvider) {
		this.serviceConfigProvider = serviceConfigProvider;
	}

	/**
	 * Gets the current instance of the access lock id provider used for
	 * uniquely identifying a given process instance (beyond what update process
	 * ids can provide)
	 */
	public AccessLockIdProvider getAccessLockIdProvider() {
		return accessLockIdProvider;
	}

	/**
	 * Sets the current provider for access lock identifiers
	 */
	public void setAccessLockIdProvider(AccessLockIdProvider accessLockIdProvider) {
		this.accessLockIdProvider = accessLockIdProvider;
	}

	/**
	 * Delegate to {@link ServiceProvider#getSqlService()} via
	 * {@link #getServiceProvider()}
	 */
	public SqlService getSqlService() {
		return getServiceProvider().getSqlService();
	}

	/**
	 * Returns the current base directory which will consult servlet paths
	 * and
	 */
	public String getBaseDirectory() {
		if (baseDirectory == null) {
			// attempt to set the base directory from the servlet path if
			// available
			try {
				baseDirectory = ReflectionUtils.invokeMethod(
						"uk.ac.ebi.proteome.util.ServletContextRetriever",
						"getServletPath");
			} catch (Exception e) {
				//If we haven't got this we expect this to break
			}
			if (baseDirectory == null) {
				baseDirectory = StringUtils.EMPTY;
			}
			baseDirectory = new File(baseDirectory).getAbsolutePath() + File.separatorChar;
		}
		return baseDirectory;
	}

	/**
	 * Sets the base directory with the given file and also checks for trailing
	 * file separators & will add one if it cannot be found
	 */
	public void setBaseDirectory(String baseDirectory) {

		if (!baseDirectory.endsWith(File.separator)) {
			baseDirectory = baseDirectory + File.separator;
		}
		this.baseDirectory = baseDirectory;
	}

	/**
	 * Transform a file path from UNIX style path into an absolute path.
	 * Relative paths and those starting with ./ have the current base directory
	 * appended, and ~/ is replaced by the user home
	 *
	 * @param path UNIX style path
	 * @return absolute path
	 */
	public String transformFilePath(String path) {
		if (!StringUtils.isEmpty(path)) {
			while (path.matches("(.*)\\$base_dir\\$(.*)")) {
				path = path.replaceAll("(.*)\\$base_dir\\$(.*)", "$1"
						+ getBaseDirectory() + "$2");
			}
			if (path.matches("^~/.*")) {
				path = System.getProperty("user.home") + File.separator
						+ path.substring(2);
			} else if (path.matches("^./.*")) {
				path = getBaseDirectory() + path.substring(2);
			} else if (path.matches("^[^~/ \t\n].*")) {
				path = getBaseDirectory() + path;
			}
		}
		return path;
	}

	/**
	 * Transforms a command and its arguments from standard UNIX notation into a
	 * form suitable for use within Java. The following constructs are replaced:
	 * <ul>
	 * <li>~/ = user.home
	 * <li>./ = working directory
	 * <li>$base_dir$ = base working directory
	 * </ul>
	 *
	 * @param command command to transform
	 * @return transformed command
	 */
	public String transformCommand(String command) {
		if (!StringUtils.isEmpty(command)) {
			while (command.matches("(.*)~/(.*)")) {
				command = command.replaceAll("(.*)~/(.*)", "$1"
						+ System.getProperty("user.home") + File.separatorChar
						+ "$2");
			}
			while (command.matches("^\\./(.*)")) {
				command = command.replaceAll("^./(.*)", getBaseDirectory()
						+ "$1");
			}
			while (command.matches("(.*\\s)\\./(.*)")) {
				command = command.replaceAll("(.*\\s)./(.*)", "$1"
						+ getBaseDirectory() + "$2");
			}
			while (command.matches("(.*\\s)\\./(.*)")) {
				command = command.replaceAll("(.*\\s)./(.*)", "$1"
						+ getBaseDirectory() + "$2");
			}
			while (command.matches("(.*)\\$base_dir\\$(.*)")) {
				command = command.replaceAll("(.*)\\$base_dir\\$(.*)", "$1"
						+ getBaseDirectory() + "$2");
			}
		}
		return command;
	}

	/**
	 * Sets the value of config file to the given parameter after passing it
	 * through {@link #transformFilePath(String)}
	 */
	public void setConfigFile(String configFile) {
		this.configFile = transformFilePath(configFile);
	}

	/**
	 * Returns the current config file location. If the location was null then
	 * this is initalised to the current property value file via
	 * {@link #setConfigFile(String)}
	 */
	public String getConfigFile() {
		if (configFile == null) {
			String configLocation = getProperty(Properties.CONFIG_FILE.getProperty(), null);
			setConfigFile(configLocation);
		}
		return configFile;
	}

	/**
	 * Get the base name of the current process
	 */
	public String getProcessName() {
		if (processName.get() == null) {
			String processName = getProperty(Properties.PROCESS_NAME, null);
			setProcessName(processName);
		}
		return processName.get();
	}

	/**
	 * Set the current process name
	 */
	public void setProcessName(String processName) {
		this.processName.set(processName);
	}

	/**
	 * Get the base name of the current update process
	 */
	public String getUpdateProcessName() {
		if (updateProcessName.get() == null) {
			String updateProcessName = getProperty(Properties.UPDATE_PROCESS_NAME, null);
			setUpdateProcessName(updateProcessName);
		}
		return updateProcessName.get();
	}

	/**
	 * Set the current update process name
	 */
	public void setUpdateProcessName(String updateProcessName) {
		this.updateProcessName.set(updateProcessName);
	}

	/**
	 * Returns the current identifier for access locks for this process. If
	 * there is not already one assigned then the code will get a new one
	 * from {@link #getAccessLockIdProvider()}
	 */
	public String getAccessLockId() {
		if(accessLockId.get() == null) {
			String id = getAccessLockIdProvider().getNewId();
			setAccessLockId(id);
		}
		return accessLockId.get();
	}

	/**
	 * Sets the access lock identifier into the inheritable thread local variable
	 */
	public void setAccessLockId(String accessLockId) {
		this.accessLockId.set(accessLockId);
	}

	/**
	 * Modify the current data directory into a subject/activity-specific form
	 * using the current process owner
	 */
	public String getDataDirectoryForProcess() {
		return getDirectoryForProcess(getConfig().getDataDirectory());
	}

	/**
	 * Modify the supplied directory into a subject/activity-specific form using
	 * the current process owner
	 *
	 * @param baseDir
	 * @return baseDir/user
	 */
	public String getDirectoryForProcess(String baseDir) {
		return baseDir + File.separatorChar + getProcessName();
	}

	/**
	 * Delegates to {@link #getPropertyLocator()} and queries it with the given
	 * key. Used as a shortcut to this code but really use the property provider
	 * interface since that increases how decoupled your code is from a
	 * {@link ServiceContext}
	 */
	public String getProperty(String propertyKey) {
		return getPropertyLocator().getProperty(propertyKey);
	}

	/**
	 * Default value version of
	 * {@link #getProperty(String)}
	 */
	public String getProperty(String propertyKey, String defaultValue) {
		return getPropertyLocator().getProperty(propertyKey, defaultValue);
	}

	/**
	 * Uses the {@link Properties} enum and it's method
	 * {@link Properties#getProperty()} against the {@link #getPropertyLocator()}
	 * object. Once again consider using the property locator directly (see
	 * {@link #getProperty(String)} for why)
	 */
	public String getProperty(Properties propertyKey) {
		return getPropertyLocator().getProperty(propertyKey.getProperty());
	}

	/**
	 * Default value version of
	 * {@link #getProperty(uk.ac.ebi.proteome.services.ServiceContext.Properties)}
	 */
	public String getProperty(Properties propertyKey, String defaultValue) {
		return getPropertyLocator().getProperty(propertyKey.getProperty(), defaultValue);
	}

	/**
	 * Allows you to set a value in the {@link #getPropertyLocator()}
	 *
	 * @param service The service enum to set (will call
	 * {@link ServicesEnum#getProperty()} on the given object)
	 * @param value The value to set
	 */
	public void setProperty(ServicesEnum service, String value) {
		getPropertyLocator().setProperty(service.getProperty(), value);
	}

	/**
	 * Useful method which sets the property using
	 * {@link #setProperty(ServicesEnum, String)} where the {@link String} is
	 * {@link Class#getName()}.
	 */
	public void setProperty(ServicesEnum service, Class<?> implementation) {
		setProperty(service, implementation.getName());
	}

	/**
	 * Thin delegator to the set method in {@link #getPropertyLocator()}
	 */
	public void setProperty(String key, String value) {
		getPropertyLocator().setProperty(key, value);
	}
}
