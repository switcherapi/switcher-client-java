package com.github.petruki.switcher.client.factory;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.domain.CriteriaResponse;
import com.github.petruki.switcher.client.domain.Switcher;
import com.github.petruki.switcher.client.domain.criteria.Domain;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.facade.ClientOfflineServiceFacade;
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
	
	public SwitcherOffline(final Map<String, Object> properties) throws SwitcherSnapshotLoadException {
		
		this.init(properties);
	}
	
	/**
	 * Initialize snapshot in memory. It priotizes if a snapshot file has been passed over an environment 
	 * snapshot file
	 * 
	 * @param properties
	 * @throws SwitcherSnapshotLoadException 
	 */
	public void init(final Map<String, Object> properties) throws SwitcherSnapshotLoadException {
		
		this.snapshotLocation = (String) properties.get(SwitcherContextParam.SNAPSHOT_LOCATION);
		this.snapshotFile = (String) properties.get(SwitcherContextParam.SNAPSHOT_FILE);
		this.environment = (String) properties.get(SwitcherContextParam.ENVIRONMENT);
		
		if (this.snapshotFile != null) {
			this.domain = SnapshotLoader.loadSnapshot(this.snapshotFile);
		} else {
			this.domain = SnapshotLoader.loadSnapshot(this.snapshotLocation, this.environment);
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

}
