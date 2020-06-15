package com.github.petruki.switcher.client.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherAPIConnectionException;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotWriteException;
import com.github.petruki.switcher.client.facade.ClientServiceFacade;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.model.criteria.Domain;
import com.github.petruki.switcher.client.model.criteria.Snapshot;
import com.github.petruki.switcher.client.utils.SnapshotLoader;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public abstract class SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherExecutor.class);
	
	protected Map<String, Object> properties;
	
	private static Map<String, Boolean> bypass = new HashMap<>();
	
	public abstract void init(final Map<String, Object> properties) throws SwitcherException;
	
	public abstract boolean executeCriteria(final Switcher switcher) throws SwitcherException;
	
	public abstract void updateContext(final Map<String, Object> properties) throws SwitcherException;
	
	public abstract boolean checkSnapshotVersion() throws SwitcherException;
	
	public abstract void updateSnapshot() throws SwitcherException;
	
	public abstract void notifyChange(final String snapshotFile);
	
	public boolean checkSnapshotVersion(final Domain domain) throws SwitcherException {
		
		final String environment = this.getEnvironment();
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("verifying snapshot version - environment: %s", environment));
		}
		
		try {
			return ClientServiceFacade.getInstance().checkSnapshotVersion(this.properties, domain.getVersion());
		} catch (SwitcherAPIConnectionException e) {
			logger.error(e);
			throw e;
		}
	}
	
	public Domain initializeSnapshotFromAPI() throws SwitcherException {
		
		final String snapshotLocation = this.getSnapshotLocation();
		final String environment = this.getEnvironment();
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("initializing snapshot from API - environment: %s", environment));
		}
		
		try {
			final Snapshot snapshot = ClientServiceFacade.getInstance().resolveSnapshot(this.properties);
			SnapshotLoader.saveSnapshot(snapshot, snapshotLocation, environment);
			
			return snapshot.getDomain();
		} catch (SwitcherAPIConnectionException | SwitcherSnapshotWriteException e) {
			logger.error(e);
			throw e;
		}
	}
	
	public String getSnapshotLocation() {
		
		return this.properties.containsKey(SwitcherContextParam.SNAPSHOT_LOCATION) ? 
				(String) this.properties.get(SwitcherContextParam.SNAPSHOT_LOCATION) : StringUtils.EMPTY;
	}
	
	public String getSnapshotFile() {
		
		return this.properties.containsKey(SwitcherContextParam.SNAPSHOT_FILE) ? 
				(String) this.properties.get(SwitcherContextParam.SNAPSHOT_FILE) : StringUtils.EMPTY;
	}
	
	public String getEnvironment() {
		
		return this.properties.containsKey(SwitcherContextParam.ENVIRONMENT) ? 
				(String) this.properties.get(SwitcherContextParam.ENVIRONMENT) : StringUtils.EMPTY;
	}
	
	public boolean isSnapshotAutoLoad() {
		
		return properties.containsKey(SwitcherContextParam.SNAPSHOT_AUTO_LOAD) &&
				(boolean) properties.get(SwitcherContextParam.SNAPSHOT_AUTO_LOAD);
	}
	
	public boolean isSilentMode() {
		
		return properties.containsKey(SwitcherContextParam.SILENT_MODE) &&
				(boolean) properties.get(SwitcherContextParam.SILENT_MODE);
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
