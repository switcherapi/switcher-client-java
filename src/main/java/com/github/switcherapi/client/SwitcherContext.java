package com.github.switcherapi.client;

import static com.github.switcherapi.client.utils.SwitcherContextParam.APIKEY;
import static com.github.switcherapi.client.utils.SwitcherContextParam.COMPONENT;
import static com.github.switcherapi.client.utils.SwitcherContextParam.CONTEXT_LOCATION;
import static com.github.switcherapi.client.utils.SwitcherContextParam.DOMAIN;
import static com.github.switcherapi.client.utils.SwitcherContextParam.ENVIRONMENT;
import static com.github.switcherapi.client.utils.SwitcherContextParam.OFFLINE_MODE;
import static com.github.switcherapi.client.utils.SwitcherContextParam.RETRY_AFTER;
import static com.github.switcherapi.client.utils.SwitcherContextParam.SILENT_MODE;
import static com.github.switcherapi.client.utils.SwitcherContextParam.SNAPSHOT_AUTO_LOAD;
import static com.github.switcherapi.client.utils.SwitcherContextParam.SNAPSHOT_FILE;
import static com.github.switcherapi.client.utils.SwitcherContextParam.SNAPSHOT_LOCATION;
import static com.github.switcherapi.client.utils.SwitcherContextParam.SNAPSHOT_SKIP_VALIDATION;
import static com.github.switcherapi.client.utils.SwitcherContextParam.URL;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import com.github.switcherapi.client.service.remote.SwitcherRemoteService;
import com.github.switcherapi.client.utils.SwitcherContextUtils;
import com.github.switcherapi.client.utils.SwitcherUtils;

/**
 * <b>Switcher Context Toolkit</b>
 * <p>
 * 
 * This class will load Switcher Properties internally, making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * 
 * <pre>
 * public class SwitcherFeatures extends SwitcherContext {
 * 	&#064;SwitcherKey
 * 	public static final String MY_FEATURE = "MY_FEATURE";
 * }
 * </pre>
 * 
 * @see SwitcherKey
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherContext {
	
	private static final Logger logger = LogManager.getLogger(SwitcherContext.class);
	
	private static final SwitcherProperties switcherProperties;
	private static Set<String> switcherKeys;
	private static Map<String, Switcher> switchers;
	private static SwitcherExecutor instance;
	
	protected SwitcherContext() {
		throw new IllegalStateException("Configuration class cannot be instantiated");
	}
	
	static {
		switcherProperties = new SwitcherProperties();
		loadProperties();
	}
	
	/**
	 * Load properties from the resources folder, look up for switcherapi.properties file.
	 * After loading the properties, it will validate the arguments and load the Switchers in memory.
	 */
	public static void loadProperties() {
		try (InputStream input = SwitcherContext.class
				.getClassLoader().getResourceAsStream("switcherapi.properties")) {
			
			Properties prop = new Properties();
            prop.load(input);
            
            switcherProperties.setContextLocation(SwitcherUtils.resolveProperties(CONTEXT_LOCATION, prop));
            switcherProperties.setUrl(SwitcherUtils.resolveProperties(URL, prop));
    		switcherProperties.setApiKey(SwitcherUtils.resolveProperties(APIKEY, prop));
    		switcherProperties.setDomain(SwitcherUtils.resolveProperties(DOMAIN, prop));
    		switcherProperties.setComponent(SwitcherUtils.resolveProperties(COMPONENT, prop));
    		switcherProperties.setEnvironment(SwitcherUtils.resolveProperties(ENVIRONMENT, prop));
    		switcherProperties.setSnapshotFile(SwitcherUtils.resolveProperties(SNAPSHOT_FILE, prop));
    		switcherProperties.setSnapshotLocation(SwitcherUtils.resolveProperties(SNAPSHOT_LOCATION, prop));
    		switcherProperties.setSnapshotSkipValidation(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SNAPSHOT_SKIP_VALIDATION, prop)));
    		switcherProperties.setSnapshotAutoLoad(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SNAPSHOT_AUTO_LOAD, prop)));
    		switcherProperties.setSilentMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SILENT_MODE, prop)));
    		switcherProperties.setOfflineMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(OFFLINE_MODE, prop)));
    		switcherProperties.setRetryAfter(SwitcherUtils.resolveProperties(RETRY_AFTER, prop));

    		initializeClient();
        } catch (IOException io) {
        	throw new SwitcherContextException(io.getMessage());
        }
	}
	
	/**
	 * Initialize Switcher Client
	 */
	public static void initializeClient() {
		validateContext();
		validateSwitcherKeys();
		
		if (switcherProperties.isOfflineMode()) {
			instance = new SwitcherLocalService();
		} else {
			instance = new SwitcherRemoteService();
		}
		
		loadSwitchers();
	}
	
	/**
	 * Verifies if the client context is valid
	 * 
	 * @throws SwitcherContextException 
	 *  If an error was found, showing then the missing parameter
	 */
	private static void validateContext() throws SwitcherContextException {
		final SwitcherProperties prop = SwitcherContext.getProperties();
		SwitcherContextUtils.validate(prop);
	}
	
	/**
	 * Validate Switcher Keys.<br>
	 * It will ensure that only properly annotated Switchers can be used.
	 */
	private static void validateSwitcherKeys() {
		try {
			switcherKeys = new HashSet<>();
			
			final Class<?> clazz = Class.forName(switcherProperties.getContextLocation());
			for (Field field : clazz.getFields()) {
				if (field.isAnnotationPresent(SwitcherKey.class)) {
					switcherKeys.add(field.getName());
				}
			}
		} catch (ClassNotFoundException e) {
			throw new SwitcherContextException(e.getMessage());
		}
	}
	
	/**
	 * Load Switcher instances into a map cache
	 */
	private static void loadSwitchers() {
		if (switchers == null)
			switchers = new HashMap<>();
		
		switchers.clear();
		for (String key : switcherKeys)
			switchers.put(key, new Switcher(key, instance));
	}
	
	/**
	 * Return a ready-to-use Switcher that will invoke the criteria configured into the Switcher API or Snapshot
	 * 
	 * @param key name of the key created
	 * @param keepEntries when true it will return a cached Switcher with all parameters used before
	 * 
	 * @return a ready to use Switcher
	 * @throws SwitcherKeyNotFoundException in case the key was not properly loaded
	 */
	public static Switcher getSwitcher(String key, boolean keepEntries) {
		if (logger.isDebugEnabled())
			logger.debug(String.format("key: %s - keepEntries: %s", key, keepEntries));
		
		if (!switchers.containsKey(key))
			throw new SwitcherKeyNotFoundException(key);
		
		final Switcher switcher = switchers.get(key);
		if (!keepEntries)
			switcher.resetEntry();
		
		return switcher;
	}
	
	/**
	 * {@link #getSwitcher(String, boolean)}
	 * 
	 * @param key name
	 * @return a ready to use Switcher
	 */
	public static Switcher getSwitcher(String key) {
		return getSwitcher(key, false);
	}
	
	/**
	 * Validate and update local snapshot file.<br>
	 * It requires offline mode or SwitcherContextParam.SNAPSHOT_LOCATION configured
	 * 
	 * @return true if validation was performed
	 */
	public static boolean validateSnapshot() {
		if (switcherProperties.isSnapshotSkipValidation())
			return false;
		
		if (!instance.checkSnapshotVersion())
			instance.updateSnapshot();
		
		return true;
	}
	
	/**
	 * Start watching snapshot files for modifications.<br>
	 * When the file is modified the in-memory snapshot will reload
	 */
	public static void watchSnapshot() {
		SwitcherUtils.watchSnapshot(instance);
	}
	
	/**
	 * Unregister snapshot location and terminates the Thread watcher
	 * 
	 * @throws SwitcherException if watch thread never started
	 */
	public static void stopWatchingSnapshot() {
		SwitcherUtils.stopWatchingSnapshot();
	}
	
	/**
	 * Executes smoke test against the API to verify if all Switchers are properly configured
	 * 
	 * @throws SwitchersValidationException when one or more Switcher Key is not found
	 */
	public static void checkSwitchers() {
		instance.checkSwitchers(switcherKeys);
	}
	
	public static SwitcherProperties getProperties() {
		return switcherProperties;
	}
	
}
