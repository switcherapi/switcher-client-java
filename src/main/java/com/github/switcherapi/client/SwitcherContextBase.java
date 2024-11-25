package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.remote.ClientWS;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.service.SwitcherValidator;
import com.github.switcherapi.client.service.ValidatorService;
import com.github.switcherapi.client.service.WorkerName;
import com.github.switcherapi.client.service.local.ClientLocal;
import com.github.switcherapi.client.service.local.ClientLocalService;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
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
import java.util.concurrent.*;

import static com.github.switcherapi.client.remote.Constants.DEFAULT_POOL_SIZE;
import static com.github.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;

/**
 * <b>Switcher Context Base</b>
 * 
 * <p>
 * This class will load Switcher Properties internally, making it ready to use.
 * By inheriting this class, all Switchers can be placed in one single place.
 * </p>
 * 
 * <p>
 * The Base context allows you to configure the Switcher Client using the {@link ContextBuilder} or
 * simply override the {@link #configureClient()} method to configure the client programmatically.
 * </p>
 * 
 * <pre>
 * public class Features extends SwitcherContextBase {
 *
 * 	&#064;SwitcherKey
 * 	public static final String MY_FEATURE = "MY_FEATURE";
 *
 *  	// Initialize the client using PostConstruct
 * 	&#064;Override
 * 	&#064;PostConstruct
 * 	public void configureClient() {
 * 		// you can add pre-configuration here
 * 		super.configureClient();
 * 		// you can add post-configuration here
 * 	}
 * }
 *
 * // Initialize the Switcher Client using ContextBuilder
 * public void configureClient() {
 *  	Features.configure(ContextBuilder.builder()
 *   		.context("com.business.config.Features")
 *   		.apiKey("API_KEY")
 *   		.domain("Playground")
 *   		.component("switcher-playground")
 *   		.environment("default"));
 *
 *  	Features.initializeClient();
 * }
 *
 * </pre>
 *
 * @see SwitcherKey
 * @author Roger Floriano (petruki)
 * @since 2022-06-19
 */
public abstract class SwitcherContextBase extends SwitcherConfig {
	
	protected static final Logger logger = LoggerFactory.getLogger(SwitcherContextBase.class);
	
	protected static SwitcherProperties switcherProperties;
	protected static Set<String> switcherKeys;
	protected static Map<String, Switcher> switchers;
	protected static SwitcherExecutor instance;
	private static ScheduledExecutorService scheduledExecutorService;
	private static ExecutorService watcherExecutorService;
	private static SnapshotWatcher watcherSnapshot;
	protected static SwitcherContextBase contextBase;
	
	static {
		switcherProperties = new SwitcherPropertiesImpl();
	}

	@Override
	protected void configureClient() {
		SwitcherContextBase.contextBase = this;

		configure(ContextBuilder.builder(true)
				.context(contextBase.getClass().getName())
				.url(url)
				.apiKey(apikey)
				.domain(domain)
				.environment(environment)
				.component(component)
				.local(local)
				.silentMode(silent)
				.timeoutMs(timeout)
				.poolConnectionSize(poolSize)
				.snapshotLocation(snapshot.getLocation())
				.snapshotAutoLoad(snapshot.isAuto())
				.snapshotSkipValidation(snapshot.isSkipValidation())
				.snapshotAutoUpdateInterval(snapshot.getUpdateInterval())
				.truststorePath(truststore.getPath())
				.truststorePassword(truststore.getPassword()));

		switcherProperties.setValue(ContextKey.CONTEXT_LOCATION, contextBase.getClass().getName());
		initializeClient();
	}

	@Override
	protected void configureClient(String contextFile) {
		SwitcherContextBase.contextBase = this;

		loadProperties(contextFile);

		switcherProperties.setValue(ContextKey.CONTEXT_LOCATION, contextBase.getClass().getName());
		initializeClient();
	}

	/**
	 * Load properties from the resources' folder, look up for a given context file name (without extension).<br>
	 * <p>
	 * Use this method optionally if you want to load the settings from properties file.<br>
	 * </p>
	 *
	 * Features must inherit {@link SwitcherContextBase}
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
		} catch (IOException io) {
			throw new SwitcherContextException(io.getMessage());
		}
	}
	
	/**
	 * Initialize Switcher Client SDK.<br>
	 *
	 * - Validate the context<br>
	 * - Validate Switcher Keys<br>
	 * - Build the Switcher Executor instance<br>
	 * - Load Switchers into memory<br>
	 * - Pre-configure the context<br>
	 */
	public static void initializeClient() {
		validateContext();
		validateSwitcherKeys();
		instance = buildInstance();
		
		loadSwitchers();
		scheduleSnapshotAutoUpdate(contextStr(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL));
		ContextBuilder.preConfigure(switcherProperties);
		SwitcherUtils.debug(logger, "Switcher Client initialized");
	}

	/**
	 * Build the Switcher Executor instance based on the context
	 *
	 * @return SwitcherExecutor instance
	 */
	private static SwitcherExecutor buildInstance() {
		final ClientWS clientWS = initRemotePoolExecutorService();
		final SwitcherValidator validatorService = new ValidatorService();
		final ClientRemote clientRemote = new ClientRemoteService(clientWS, switcherProperties);
		final ClientLocal clientLocal = new ClientLocalService(validatorService);

		if (contextBol(ContextKey.LOCAL_MODE)) {
			return new SwitcherLocalService(clientRemote, clientLocal, switcherProperties);
		} else {
			return new SwitcherRemoteService(clientRemote, new SwitcherLocalService(clientRemote, clientLocal, switcherProperties));
		}
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
		if (Objects.nonNull(contextBase)) {
			registerSwitcherKey(contextBase.getClass().getFields());
		} else {
			try {
				final Class<?> clazz = Class.forName(contextStr(ContextKey.CONTEXT_LOCATION));
				registerSwitcherKey(clazz.getFields());
			} catch(ClassNotFoundException e){
				throw new SwitcherContextException(e.getMessage());
			}
		}
	}

	/**
	 * Register Switcher Keys based on the annotation {@link SwitcherKey}
	 *
	 * @param fields to be registered
	 */
	private static void registerSwitcherKey(Field[] fields) {
		switcherKeys = new HashSet<>();
		for (Field field : fields) {
			if (field.isAnnotationPresent(SwitcherKey.class)) {
				switcherKeys.add(field.getName());
			}
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
	 * @return ScheduledFuture instance
	 */
	public static ScheduledFuture<?> scheduleSnapshotAutoUpdate(String intervalValue, SnapshotCallback callback) {
		if (StringUtils.isBlank(intervalValue) || scheduledExecutorService != null) {
			return null;
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
		return scheduledExecutorService.scheduleAtFixedRate(runnableSnapshotValidate, 0, interval, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedule a task to update the snapshot automatically.<br>
	 * The task will be executed in a single thread executor service.
	 *
	 * @param intervalValue to be used for the update (e.g. 5s, 1m, 1h, 1d)
	 * @return ScheduledFuture instance
	 */
	public static ScheduledFuture<?> scheduleSnapshotAutoUpdate(String intervalValue) {
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
	 * Configure Executor Service for Switcher Remote Worker
	 */
	private static ClientWS initRemotePoolExecutorService() {
		int timeoutMs = Optional.ofNullable(contextInt(ContextKey.TIMEOUT_MS)).orElse(DEFAULT_TIMEOUT);
		int poolSize = Optional.ofNullable(contextInt(ContextKey.POOL_CONNECTION_SIZE)).orElse(DEFAULT_POOL_SIZE);
		String component = Optional.ofNullable(contextStr(ContextKey.COMPONENT)).orElse("switcher-client");

		final ExecutorService remotePoolExecutorService = Executors.newFixedThreadPool(poolSize, r -> {
			Thread thread = new Thread(r);
			thread.setName(String.format("%s-%s", WorkerName.SWITCHER_REMOTE_WORKER, component));
			thread.setDaemon(true);
			return thread;
		});

		return ClientWSImpl.build(switcherProperties, remotePoolExecutorService, timeoutMs);
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
		if (contextBol(ContextKey.SNAPSHOT_SKIP_VALIDATION) || instance.checkSnapshotVersion()) {
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

		if (watcherSnapshot == null) {
			watcherSnapshot = new SnapshotWatcher((SwitcherLocalService) instance, handler,
					contextStr(ContextKey.SNAPSHOT_LOCATION));
		}

		initWatcherExecutorService();
		watcherExecutorService.submit(watcherSnapshot);
	}
	
	/**
	 * Unregister snapshot location and terminates the Thread watcher
	 * 
	 * @throws SwitcherException if watch thread never started
	 */
	public static void stopWatchingSnapshot() {
		if (watcherSnapshot != null) {
			watcherExecutorService.shutdownNow();
			watcherSnapshot.terminate();
			watcherSnapshot = null;
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
		return switcherProperties.getValue(contextKey);
	}

	/**
	 * Retrieve integer context parameter based on contextKey
	 *
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static Integer contextInt(ContextKey contextKey) {
		return switcherProperties.getInt(contextKey);
	}
	
	/**
	 * Retrieve boolean context parameter based on contextKey
	 * 
	 * @param contextKey to be retrieved
	 * @return Value configured for the context parameter
	 */
	public static boolean contextBol(ContextKey contextKey) {
		return switcherProperties.getBoolean(contextKey);
	}

	/**
	 * Retrieve the Switcher Properties
	 *
	 * @return SwitcherProperties instance
	 */
	public static SwitcherProperties getSwitcherProperties() {
		return switcherProperties;
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
