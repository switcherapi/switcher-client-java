package com.github.petruki.switcher.client.factory;

import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherAPIConnectionException;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotWriteException;
import com.github.petruki.switcher.client.facade.ClientOfflineServiceFacade;
import com.github.petruki.switcher.client.facade.ClientServiceFacade;
import com.github.petruki.switcher.client.model.CriteriaResponse;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.model.criteria.Domain;
import com.github.petruki.switcher.client.model.criteria.Snapshot;
import com.github.petruki.switcher.client.utils.SnapshotLoader;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOffline implements SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOffline.class);
	
	private String snapshotFile;
	
	private String snapshotLocation;
	
	private String environment;
	
	private Domain domain;
	
	public SwitcherOffline(final Map<String, Object> properties) throws SwitcherException {
		
		this.init(properties);
	}
	
	/**
	 * Initialize snapshot in memory. It priotizes direct file path over environment based snapshot
	 * 
	 * @param properties
	 * @throws SwitcherSnapshotLoadException 
	 */
	public void init(final Map<String, Object> properties) throws SwitcherException {
		
		this.snapshotLocation = (String) properties.get(SwitcherContextParam.SNAPSHOT_LOCATION);
		this.snapshotFile = (String) properties.get(SwitcherContextParam.SNAPSHOT_FILE);
		this.environment = (String) properties.get(SwitcherContextParam.ENVIRONMENT);
		
		if (this.snapshotFile != null) {
			this.domain = SnapshotLoader.loadSnapshot(this.snapshotFile);
		} else {
			try {
				this.domain = SnapshotLoader.loadSnapshot(this.snapshotLocation, this.environment);
			} catch (FileNotFoundException e) {
				if (properties.containsKey(SwitcherContextParam.SNAPSHOT_AUTO_LOAD) &&
						(boolean) properties.get(SwitcherContextParam.SNAPSHOT_AUTO_LOAD)) {
					this.initializeSnapshotFromAPI(properties);
				} else {
					throw new SwitcherSnapshotLoadException(String.format("%s/%s.json", snapshotLocation, environment), e);
				}
			}
		}
	}
	
	@Override
	public boolean executeCriteria(final Switcher switcher) throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final CriteriaResponse response = ClientOfflineServiceFacade.getInstance().executeCriteria(switcher, this.domain);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("[Offline] response: %s", response));
		}
		
		return response.isItOn();
	}
	
	@Override
	public void updateContext(final Map<String, Object> properties) throws SwitcherException {
		
		this.init(properties);
	}
	
	/**
	 * Load snapshot, save into a file and load in the memory
	 * 
	 * @param properties
	 * @throws SwitcherException
	 */
	private void initializeSnapshotFromAPI(final Map<String, Object> properties) throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("initializing snapshot from API - environment: %s", this.environment));
		}
		
		try {
			final Snapshot snapshot = ClientServiceFacade.getInstance().resolveSnapshot(properties);
			SnapshotLoader.saveSnapshot(snapshot, this.snapshotLocation, this.environment);
			
			this.domain = snapshot.getDomain();
		} catch (SwitcherAPIConnectionException | SwitcherSnapshotWriteException e) {
			logger.error(e);
			throw e;
		}
	}

}
