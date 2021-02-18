package com.github.switcherapi.client.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.configuration.SwitcherProperties;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.facade.ClientServiceFacade;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.utils.SnapshotLoader;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public abstract class SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherExecutor.class);
	
	private static Map<String, Boolean> bypass = new HashMap<>();
	
	public abstract CriteriaResponse executeCriteria(final Switcher switcher);
	
	public abstract boolean checkSnapshotVersion();
	
	public abstract void updateSnapshot();
	
	public abstract void notifyChange(final String snapshotFile);
	
	public boolean checkSnapshotVersion(final Domain domain) {
		
		final String environment = SwitcherContext.getProperties().getEnvironment();
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("verifying snapshot version - environment: %s", environment));
		}
		
		try {
			return ClientServiceFacade.getInstance().checkSnapshotVersion(domain.getVersion());
		} catch (SwitcherException e) {
			logger.error(e);
			throw e;
		}
	}
	
	public Domain initializeSnapshotFromAPI() {
		
		final SwitcherProperties properties = SwitcherContext.getProperties();

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("initializing snapshot from API - environment: %s", properties.getEnvironment()));
		}
		
		try {
			final Snapshot snapshot = ClientServiceFacade.getInstance().resolveSnapshot();
			SnapshotLoader.saveSnapshot(snapshot, properties.getSnapshotLocation(), properties.getEnvironment());
			
			return snapshot.getDomain();
		} catch (SwitcherAPIConnectionException | SwitcherSnapshotWriteException e) {
			logger.error(e);
			throw e;
		}
	}
	
	/**
	 * It manipulates the result of a given key.
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
