package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.utils.SnapshotEventHandler;
import com.github.switcherapi.client.utils.SnapshotLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

	/**
	 * Retrieve local snapshot version
	 *
	 * @return snapshot version
	 */
	public abstract long getSnapshotVersion();
	
	protected boolean checkSnapshotVersion(ClientRemote clientRemote, final Domain domain) {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		logger.debug("verifying snapshot version - environment: {}", environment);
		
		return clientRemote.checkSnapshotVersion(domain.getVersion());
	}
	
	protected Domain initializeSnapshotFromAPI(ClientRemote clientRemote) {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		logger.debug("initializing snapshot from API - environment: {}", environment);
		
		try {
			final Snapshot snapshot = clientRemote.resolveSnapshot();
			final String snapshotLocation = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION);

			if (snapshotLocation != null) {
				SnapshotLoader.saveSnapshot(snapshot, snapshotLocation, environment);
			}
			
			return snapshot.getDomain();
		} catch (SwitcherRemoteException | SwitcherSnapshotWriteException e) {
			logger.error(e);
			throw e;
		}
	}
	
	/**
	 * It manipulates the result of a given Switcher key.
	 * 
	 * @param key name of the key that you want to change the result
	 * @param expectedResult that will be returned when performing isItOn
	 */
	public static void assume(final String key, boolean expectedResult) {
		bypass.put(key, expectedResult);
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
