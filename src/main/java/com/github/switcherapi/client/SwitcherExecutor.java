package com.github.switcherapi.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import com.github.switcherapi.client.utils.SnapshotEventHandler;
import com.github.switcherapi.client.utils.SnapshotLoader;

/**
 * The Executor provides an API to handle Remote and Local functionalities that
 * should be available for both Services implementations.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public abstract class SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherExecutor.class);
	
	private static final Map<String, Boolean> bypass = new HashMap<>();
	
	/**
	 * Execute criteria based on the Switcher configuration
	 * 
	 * @param switcher to be evaluated
	 * @return Criteria response containing the evaluation details
	 */
	public abstract CriteriaResponse executeCriteria(final Switcher switcher);
	
	/**
	 * Check the snapshot versions against the Remote configuration.
	 * 
	 * @return True if snapshot is up-to-date
	 */
	public abstract boolean checkSnapshotVersion();
	
	/**
	 * Retrieve updated snapshot from the remote API
	 */
	public abstract void updateSnapshot();
	
	/**
	 * Check set of Switchers if they are properly configured.
	 * 
	 * @param switchers To be validated
	 */
	public abstract void checkSwitchers(final Set<String> switchers);
	
	/**
	 * Update in-memory snapshot.
	 * 
	 * @param snapshotFile Path location
	 * @param handler to notify snapshot change events
	 * 
	 * @return true if valid change
	 */
	public abstract boolean notifyChange(final String snapshotFile, SnapshotEventHandler handler);
	
	protected boolean checkSnapshotVersion(final Domain domain) {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("verifying snapshot version - environment: %s", environment));
		}
		
		return ClientRemoteService.getInstance().checkSnapshotVersion(domain.getVersion());
	}
	
	protected Domain initializeSnapshotFromAPI() {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("initializing snapshot from API - environment: %s", environment));
		}
		
		try {
			final Snapshot snapshot = ClientRemoteService.getInstance().resolveSnapshot();
			SnapshotLoader.saveSnapshot(snapshot, 
					SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION), 
					environment);
			
			return snapshot.getDomain();
		} catch (SwitcherAPIConnectionException | SwitcherSnapshotWriteException e) {
			logger.error(e);
			throw e;
		}
	}
	
	/**
	 * It manipulates the result of a given Switcher key.
	 * 
	 * @param key name of the key that you want to change the result
	 * @param expepectedResult result that will be returned when performing isItOn
	 */
	public static void assume(final String key, boolean expepectedResult) {
		bypass.put(key, expepectedResult);
	}
	
	/**
	 * It will clean up any result manipulation added before by invoking {@link SwitcherExecutor#assume(String, boolean)}
	 * 
	 * @param key name of the key you want to remove
	 */
	public static void forget(final String key) {
		bypass.remove(key);
	}

	public static Map<String, Boolean> getBypass() {
		return bypass;
	}
}
