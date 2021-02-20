package com.github.switcherapi.client.configuration;

import static com.github.switcherapi.client.utils.SwitcherContextParam.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.github.switcherapi.client.factory.SwitcherOffline;
import com.github.switcherapi.client.factory.SwitcherOnline;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.github.switcherapi.client.ws.ClientWS;

/**
 * <b>Switcher Context Loader</b>
 * <p>
 * 
 * This class will load Switcher Properties internally making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * 
 * <p>
 * Annotate your property with @SwitcherKey
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherContext {
	
	private static final Logger logger = LogManager.getLogger(SwitcherContext.class);
	
	private static final String ENV_VARIABLE_PATTERN = "\\$\\{(\\w+)\\}";
	private static final SwitcherProperties switcherProperties;
	private static Set<String> switchers;
	private static SwitcherExecutor instance;
	
	protected SwitcherContext() {
		logger.debug("Context initialized");
	}
	
	static {
		switcherProperties = new SwitcherProperties();
		loadProperties();
	}
	
	public static void loadProperties() {
		try (InputStream input = SwitcherContext.class
				.getClassLoader().getResourceAsStream("switcherapi.properties")) {
			
			Properties prop = new Properties();
            prop.load(input);
            
            switcherProperties.setContextLocation(resolveProperties(CONTEXT_LOCATION, prop));
            switcherProperties.setUrl(resolveProperties(URL, prop));
    		switcherProperties.setApiKey(resolveProperties(APIKEY, prop));
    		switcherProperties.setDomain(resolveProperties(DOMAIN, prop));
    		switcherProperties.setComponent(resolveProperties(COMPONENT, prop));
    		switcherProperties.setEnvironment(resolveProperties(ENVIRONMENT, prop));
    		switcherProperties.setSnapshotFile(resolveProperties(SNAPSHOT_FILE, prop));
    		switcherProperties.setSnapshotLocation(resolveProperties(SNAPSHOT_LOCATION, prop));
    		switcherProperties.setSnapshotAutoLoad(Boolean.parseBoolean(resolveProperties(SNAPSHOT_AUTO_LOAD, prop)));
    		switcherProperties.setSilentMode(Boolean.parseBoolean(resolveProperties(SILENT_MODE, prop)));
    		switcherProperties.setOfflineMode(Boolean.parseBoolean(resolveProperties(OFFLINE_MODE, prop)));
    		switcherProperties.setRetryAfter(resolveProperties(RETRY_AFTER, prop));

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
		
		SwitcherExecutor.getBypass().clear();
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
	 * Resolve properties from switcherapi.properties file.
	 * It reads environment values when using the following notation: ${VALUE}
	 * 
	 * @param input reads values from {@link SwitcherContextParam}
	 * @param prop from properties file
	 * @return resolved value
	 */
	private static String resolveProperties(String input, Properties prop) {
		final String value = prop.getProperty(input);
		
	    if (StringUtils.isBlank(value)) {
	        return null;
	    }

	    Pattern pattern = Pattern.compile(ENV_VARIABLE_PATTERN);
	    Matcher matcher = pattern.matcher(value);
	    StringBuffer sBuffer = new StringBuffer();
	    
	    while(matcher.find()){
	        String envVarName = matcher.group(1).isBlank() ? matcher.group(2) : matcher.group(1);
	        String envVarValue = System.getenv(envVarName);
	        matcher.appendReplacement(sBuffer, null == envVarValue ? StringUtils.EMPTY : envVarValue);
	    }
	    
	    if (sBuffer.toString().isEmpty())
	    	return value;
	       
	    return sBuffer.toString();
	}
	
	/**
	 * Return a ready-to-use Switcher that will invoke the criteria configured into the Switcher API or Snapshot
	 * 
	 * @param key name of the key created
	 * @return a ready to use Switcher
	 * @throws SwitcherKeyNotFoundException in case the key was not properly loaded
	 * @throws SwitcherContextException in case context not loaded properly
	 */
	public static Switcher getSwitcher(String key) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("key: %s", key));
		}
		
		if (instance == null) {
			throw new SwitcherContextException();
		}
		
		if (!switchers.contains(key)) {
			throw new SwitcherKeyNotFoundException(key);
		}
		
		return new Switcher(key, instance);
	}
	
	/**
	 * Validate and update local snapshot file
	 * It requires offline mode or SwitcherContextParam.SNAPSHOT_LOCATION configured
	 * 
	 * @throws SwitcherException
	 *  If an error has occrured when invoking {@link ClientWS#SNAPSHOT_URL} and {@link ClientWS#SNAPSHOT_VERSION_CHECK}
	 */
	public static void validateSnapshot() {
		if (instance == null) {
			throw new SwitcherContextException();
		}
		
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
