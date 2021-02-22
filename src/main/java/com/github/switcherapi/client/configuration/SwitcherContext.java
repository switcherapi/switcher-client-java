package com.github.switcherapi.client.configuration;

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
import static com.github.switcherapi.client.utils.SwitcherContextParam.URL;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.github.switcherapi.client.factory.SwitcherOffline;
import com.github.switcherapi.client.factory.SwitcherOnline;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.utils.SwitcherUtils;

/**
 * <b>Switcher Context Toolkit</b>
 * <p>
 * 
 * This class will load Switcher Properties internally, making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * 
 * <p>
 * Annotate your property with @SwitcherKey
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherContext {
	
	private static final Logger logger = LogManager.getLogger(SwitcherContext.class);
	
	private static final SwitcherProperties switcherProperties;
	private static Set<String> switchers;
	private static SwitcherExecutor instance;
	
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
		loadSwitchers();
		
		if (switcherProperties.isOfflineMode()) {
			instance = new SwitcherOffline();
		} else {
			instance = new SwitcherOnline();
		}
	}
	
	/**
	 * Verifies if the client context is valid
	 * 
	 * @throws SwitcherContextException 
	 *  If an error was found, showing then the missing parameter
	 */
	private static void validateContext() 
			throws SwitcherContextException {
		
		final SwitcherProperties prop = SwitcherContext.getProperties();
		if (!switcherProperties.isOfflineMode()) {
			if (StringUtils.isBlank(prop.getUrl())) {
				throw new SwitcherContextException("SwitcherContextParam.URL not found");
			}
			
			if (StringUtils.isBlank(prop.getApiKey())) {
				throw new SwitcherContextException("SwitcherContextParam.APIKEY not found");
			}
			
			if (StringUtils.isBlank(prop.getDomain())) {
				throw new SwitcherContextException("SwitcherContextParam.DOMAIN not found");
			}
			
			if (StringUtils.isBlank(prop.getComponent())) {
				throw new SwitcherContextException("SwitcherContextParam.COMPONENT not found");
			}
		}
		
		if (prop.isSnapshotAutoLoad() && StringUtils.isBlank(prop.getSnapshotLocation())) {
			throw new SwitcherContextException("SwitcherContextParam.SNAPSHOT_LOCATION not found");
		}
		
		if (prop.isSilentMode() && StringUtils.isBlank(prop.getRetryAfter())) {
			throw new SwitcherContextException("SwitcherContextParam.RETRY_AFTER not found");
		}
	}
	
	/**
	 * Prevalidate Switchers.
	 * It will ensure that only properly annotated Switchers can be used.
	 */
	private static void loadSwitchers() {
		try {
			switchers = new HashSet<>();
			
			final Class<?> clazz = Class.forName(switcherProperties.getContextLocation());
			for (Field field : clazz.getFields()) {
				if (field.isAnnotationPresent(SwitcherKey.class)) {
					switchers.add(field.getName());
				}
			}
		} catch (ClassNotFoundException e) {
			throw new SwitcherContextException(e.getMessage());
		}
	}
	
	/**
	 * Return a ready-to-use Switcher that will invoke the criteria configured into the Switcher API or Snapshot
	 * 
	 * @param key name of the key created
	 * @return a ready to use Switcher
	 * @throws SwitcherKeyNotFoundException in case the key was not properly loaded
	 */
	public static Switcher getSwitcher(String key) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("key: %s", key));
		}
		
		if (!switchers.contains(key)) {
			throw new SwitcherKeyNotFoundException(key);
		}
		
		return new Switcher(key, instance);
	}
	
	/**
	 * Validate and update local snapshot file
	 * It requires offline mode or SwitcherContextParam.SNAPSHOT_LOCATION configured
	 */
	public static void validateSnapshot() {
		if (!instance.checkSnapshotVersion()) {
			instance.updateSnapshot();
		}
	}
	
	/**
	 * Start watching snapshot files for modifications. As it has changed, it will update the domain in memory
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
	
	public static SwitcherProperties getProperties() {
		return switcherProperties;
	}
	
}
