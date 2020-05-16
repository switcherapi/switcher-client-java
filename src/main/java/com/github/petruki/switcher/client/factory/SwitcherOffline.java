package com.github.petruki.switcher.client.factory;

import java.io.FileNotFoundException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.facade.ClientOfflineServiceFacade;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.model.criteria.Domain;
import com.github.petruki.switcher.client.model.response.CriteriaResponse;
import com.github.petruki.switcher.client.utils.SnapshotLoader;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOffline extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOffline.class);
	
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
	@Override
	public void init(final Map<String, Object> properties) throws SwitcherException {
		
		this.properties = properties;
		
		final String snapshotFile = super.getSnapshotFile();
		final String snapshotLocation = super.getSnapshotLocation();
		final String environment = super.getEnvironment();
		
		if (StringUtils.isNotBlank(snapshotFile)) {
			this.domain = SnapshotLoader.loadSnapshot(snapshotFile);
		} else {
			try {
				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
			} catch (FileNotFoundException e) {
				if (super.isSnapshotAutoLoad()) {
					this.domain = this.initializeSnapshotFromAPI();
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
	public boolean checkSnapshotVersion() throws SwitcherException {
		
		return super.checkSnapshotVersion(this.domain);
	}

	@Override
	public void updateSnapshot() throws SwitcherException {
		
		this.domain = super.initializeSnapshotFromAPI();
	}
	
	@Override
	public void notifyChange(final String snapshotFile) {
		
		final String environment = super.getEnvironment();
		final String snapshotLocation = super.getSnapshotLocation();
		
		try {
			if (snapshotFile.equals(String.format("%s.json", environment))) {
				logger.debug("Updating domain");
				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
			}
		} catch (SwitcherSnapshotLoadException | FileNotFoundException e) {
			logger.error(e);
		}
	}
	
	@Override
	public void updateContext(final Map<String, Object> properties) throws SwitcherException {
		
		this.init(properties);
	}

}
