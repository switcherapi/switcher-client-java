package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.service.WorkerName;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import com.github.switcherapi.client.service.remote.SwitcherRemoteService;
import com.github.switcherapi.client.utils.SnapshotEventHandler;
import com.github.switcherapi.client.utils.SnapshotWatcher;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <b>Switcher Context Base Toolkit</b>
 * 
 * <p>
 * This class will load Switcher Properties internally, making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * </p>
 * 
 * <p>
 * The Base implementation does not initialize the SDK automatically as it should be
 * used by applications that requires the context to be loaded from an external source.
 * </p>
 * 
 * <pre>
 * public class Features extends SwitcherContextBase {
 * 	&#064;SwitcherKey
 * 	public static final String MY_FEATURE = "MY_FEATURE";
 * }
 * 
 * public void initializeSwitchers() {
 *  	Features.configure(ContextBuilder.builder()
 *   		.contextLocation("com.business.config.Features")
 *   		.apiKey("API_KEY")
 *   		.domain("Playground")
 *   		.component("switcher-playground")
 *   		.environment("default"));
 *    
 *  	Features.initializeClient();
 * }
 * </pre>
 * 
 * @see SwitcherKey
 * @author Roger Floriano (petruki)
 * @since 2022-06-19
 */
public abstract class SwitcherContextBase {
	
	protected static final Logger logger = LoggerFactory.getLogger(SwitcherContextBase.class);
	
	protected static SwitcherProperties switcherProperties;
	protected static Set<String> switcherKeys;
	protected static Map<String, Switcher> switchers;
	protected static SwitcherExecutor instance;
	private static ScheduledExecutorService scheduledExecutorService;
	private static ExecutorService watcherExecutorService;
	private static SnapshotWatcher watcher;
	
	protected SwitcherContextBase() {
		throw new IllegalStateException("Context class cannot be instantiated");
	}
	
	static {
		switcherProperties = new SwitcherProperties();
	}
	
	/**
	 * Load properties from the resources' folder, look up for a given context file name (without extension).<br>
	 * After loading the properties, it will validate the arguments and load the Switchers in memory.
	 * <p>
	 * Use this method optionally if you want to load the settings from a customized file name.
	 * </p>
	 * <pre>
	 * // Load from resources/switcherapi-test.properties 
	 * Features.loadProperties("switcherapi-test");
	 * </pre>
	 * @param contextFilename to load properties from
	 */
	public static void loadProperties(String contextFilename) {
		try (InputStream input = SwitcherContextBase.class
				.getClassLoader().getResourceAsStream(String.format("%s.properties", contextFilename))) {
			
			Properties prop = new Properties();
            prop.load(input);
            
            switcherProperties.loadFromProperties(prop);
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
		
		if (switcherProperties.isLocal()) {
			instance = new SwitcherLocalService();
		} else {
			instance = new SwitcherRemoteService();
		}
		
		loadSwitchers();
		scheduleSnapshotAutoUpdate(switcherProperties.getSnapshotAutoUpdateInterval());
		ContextBuilder.preConfigure(switcherProperties);
	}
	
	/**
	 * Verifies if the client context is valid
	 * 
	 * @throws SwitcherContextException 
	 *  If an error was found, showing then the missing parameter
	 */
	private static void validateContext() throws SwitcherContextException {
		final SwitcherProperties prop = switcherProperties;
		SwitcherContextValidator.validate(prop);
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
		if (switchers == null) {
			switchers = new HashMap<>();
		}
		
		switchers.clear();
		for (String key : switcherKeys) {
			switchers.put(key, new Switcher(key, instance));
		}
	}

	/**
	 * Schedule a task to update the snapshot automatically.<br>
	 * The task will be executed in a single thread executor service.
	 *
	 * @param intervalValue to be used for the update (e.g. 5s, 1m, 1h, 1d)
	 * @param callback to be invoked when the snapshot is updated or when an error occurs
	 * @return true if the task was scheduled successfully
	 */
	public static boolean scheduleSnapshotAutoUpdate(String intervalValue, SnapshotCallback callback) {
		if (StringUtils.isBlank(intervalValue) || scheduledExecutorService != null) {
			return false;
		}

		final long interval = SwitcherUtils.getMillis(intervalValue);
		final SnapshotCallback callbackFinal = Optional.ofNullable(callback).orElse(new SnapshotCallback() {});
		final Runnable runnableSnapshotValidate = () -> {
			try {
				if (validateSnapshot()) {
					callbackFinal.onSnapshotUpdate(instance.getSnapshotVersion());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				callbackFinal.onSnapshotUpdateError(e);
			}
		};

		initSnapshotExecutorService();
		scheduledExecutorService.scheduleAtFixedRate(runnableSnapshotValidate, 0, interval, TimeUnit.MILLISECONDS);
		return true;
	}

	/**
	 * Schedule a task to update the snapshot automatically.<br>
	 * The task will be executed in a single thread executor service.
	 *
	 * @param intervalValue to be used for the update (e.g. 5s, 1m, 1h, 1d)
	 * @return true if the task was scheduled successfully
	 */
	public static boolean scheduleSnapshotAutoUpdate(String intervalValue) {
		return scheduleSnapshotAutoUpdate(intervalValue, null);
	}

	/**
	 * Configure Executor Service for Snapshot Update Worker
	 */
	private static void initSnapshotExecutorService() {
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setName(WorkerName.SNAPSHOT_UPDATE_WORKER.toString());
			thread.setDaemon(true);
			return thread;
		});
	}

	/**
	 * Configure Executor Service for Snapshot Watch Worker
	 */
	private static void initWatcherExecutorService() {
		watcherExecutorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = new Thread(r);
			thread.setName(WorkerName.SNAPSHOT_WATCH_WORKER.toString());
			thread.setDaemon(true);
			return thread;
		});
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
		SwitcherUtils.debug(logger, "key: {} - keepEntries: {}", key, keepEntries);
		
		if (!switchers.containsKey(key)) {
			throw new SwitcherKeyNotFoundException(key);
		}
		
		final Switcher switcher = switchers.get(key);
		if (!keepEntries) {
			switcher.resetEntry();
		}
		
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
	 * Validate if the snapshot version is the same as the one in the API.<br>
	 * If the version is different, it will update the snapshot in memory.
	 * 
	 * @return true if snapshot was updated
	 */
	public static boolean validateSnapshot() {
		if (switcherProperties.isSnapshotSkipValidation() || instance.checkSnapshotVersion()) {
			return false;
		}

		instance.updateSnapshot();
		return true;
	}
	
	/**
	 * Start watching snapshot files for modifications.<br>
	 * When the file is modified the in-memory snapshot will reload
	 *
	 * <p>
	 *     (*) Requires client to use local settings
	 */
	public static void watchSnapshot() {
		watchSnapshot(new SnapshotEventHandler() {});
	}
	
	/**
	 * Start watching snapshot files for modifications.<br>
	 * When the file is modified the in-memory snapshot will reload
	 *
	 * <p>
	 *     (*) Requires client to use local settings
	 * 
	 * @param handler to notify snapshot change events
	 * @throws SwitcherException if using remote service
	 */
	public static void watchSnapshot(SnapshotEventHandler handler) {
		if (!(instance instanceof SwitcherLocalService)) {
			throw new SwitcherException("Cannot watch snapshot when using remote", new UnsupportedOperationException());
		}

		if (watcher == null) {
			watcher = new SnapshotWatcher((SwitcherLocalService) instance, handler,
					contextStr(ContextKey.SNAPSHOT_LOCATION));
		}

		initWatcherExecutorService();
		watcherExecutorService.submit(watcher);
	}
	
	/**
	 * Unregister snapshot location and terminates the Thread watcher
	 * 
	 * @throws SwitcherException if watch thread never started
	 */
	public static void stopWatchingSnapshot() {
		if (watcher != null) {
			watcherExecutorService.shutdownNow();
			watcher.terminate();
			watcher = null;
		}
	}
	
	/**
	 * Executes smoke test against the API to verify if all Switchers are properly configured
	 * 
	 * @throws SwitchersValidationException when one or more Switcher Key is not found
	 */
	public static void checkSwitchers() {
		instance.checkSwitchers(switcherKeys);
	}
	
	/**
	 * Retrieve string context parameter based on contextKey
	 * 
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static String contextStr(ContextKey contextKey) {
		return switcherProperties.getValue(contextKey, String.class);
	}
	
	/**
	 * Retrieve boolean context parameter based on contextKey
	 * 
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static boolean contextBol(ContextKey contextKey) {
		return switcherProperties.getValue(contextKey, Boolean.class);
	}
	
	/**
	 * Fluent builder to configure the Switcher Context
	 * 
	 * @param builder specification to be applied
	 */
	public static void configure(ContextBuilder builder) {
		switcherProperties = builder.build();
	}

	/**
	 * Cancel existing scheduled task for updating local Snapshot
	 */
	public static void terminateSnapshotAutoUpdateWorker() {
		if (scheduledExecutorService != null) {
			scheduledExecutorService.shutdownNow();
			scheduledExecutorService = null;
		}
	}
	
}
