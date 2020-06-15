package com.github.petruki.switcher.client;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.exception.SwitcherFactoryContextException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.factory.SwitcherExecutor;
import com.github.petruki.switcher.client.factory.SwitcherOffline;
import com.github.petruki.switcher.client.factory.SwitcherOnline;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.service.ClientService;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;
import com.github.petruki.switcher.client.utils.SwitcherUtils;

/**
 * Configure context (using {@link #buildContext(Map, boolean)} and claim switcher (using {@link #getSwitcher(String)} by using this factory.
 * 
 * @author rogerio
 * @since 2019-12-24
 * 
 * @see #buildContext(Map, boolean)
 * @see #getSwitcher(String)
 */
public class SwitcherFactory {
	
	private static final Logger logger = LogManager.getLogger(SwitcherFactory.class);
	
	private static SwitcherExecutor instance;
	
	private SwitcherFactory() {}
	
	/**
	 * Configure the context by populating these required informations
	 * 
	 * @param properties 
	 * <br> <b>Required</b>
	 * <br>	{@link SwitcherContextParam#URL}
	 * <br> {@link SwitcherContextParam#APIKEY}
	 * <br> {@link SwitcherContextParam#DOMAIN}
	 * <br> {@link SwitcherContextParam#COMPONENT}
	 * <br> {@link SwitcherContextParam#ENVIRONMENT}
	 * <br>
	 * <br> <b>Optional</b>
	 * <br> {@link SwitcherContextParam#SNAPSHOT_LOCATION}
	 * <br> {@link SwitcherContextParam#SNAPSHOT_FILE}
	 * <br> {@link SwitcherContextParam#SILENT_MODE}
	 * <br> {@link SwitcherContextParam#RETRY_AFTER}
	 * <br>
	 * @param offline If set to true, this client will find the configuration inside the configured snapshot file
	 * @throws SwitcherSnapshotLoadException in case it was not possible to load snapshot
	 * 
	 * @see SwitcherContextParam
	 */
	public static void buildContext(final Map<String, Object> properties, boolean offline)  throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("properties: %s", properties));
			logger.debug(String.format("offline: %s", offline));
		}
		
		if (instance == null) {
			if (offline) {
				instance = new SwitcherOffline(properties);
			} else {
				instance = new SwitcherOnline(properties);
			}
		} else {
			instance.updateContext(properties);
			SwitcherExecutor.getBypass().clear();
		}
	}
	
	/**
	 * Return a ready-to-use Switcher that will invoke the criteria configured into the Switcher API or Snapshot
	 * 
	 * @param key name of the key created
	 * @return a ready to use Switcher
	 * @throws SwitcherFactoryContextException If the context hasn't been configured. To avoid this exception, it's required to invoke {@link #buildContext(Map, boolean)} first
	 */
	public static Switcher getSwitcher(final String key) throws SwitcherFactoryContextException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("key: %s", key));
		}
		
		if (instance == null) {
			throw new SwitcherFactoryContextException();
		}
		
		return new Switcher(key, instance);
	}
	
	/**
	 * Validate and update local snapshot file
	 * 
	 * @throws SwitcherException
	 *  If an error has occrured when invoking {@link ClientService#SNAPSHOT_URL} and {@link ClientService#SNAPSHOT_VERSION_CHECK}
	 */
	public static void validateSnapshot() throws SwitcherException {
		
		if (instance == null) {
			throw new SwitcherFactoryContextException();
		}
		
		if (!instance.checkSnapshotVersion()) {
			instance.updateSnapshot();
		}
	}
	
	/**
	 * Start watching snapshot file for changes. As it has changed, it will update the domain in memory
	 */
	public static void watchSnapshot() {
		
		SwitcherUtils.watchSnapshot(instance);
	}
	
	/**
	 * Unregister snapshot location and terminates the Thread watcher
	 * 
	 * @throws SwitcherException if watch thread never started
	 */
	public static void stopWatchingSnapshot() throws SwitcherException {
		
		SwitcherUtils.stopWatchingSnapshot();
	}

}
