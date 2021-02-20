package com.github.switcherapi.client.factory;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.facade.ClientOfflineServiceFacade;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherProperties;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.utils.SnapshotLoader;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherOffline extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOffline.class);
	
	private Domain domain;
	
	public SwitcherOffline() {
		this.init();
	}
	
	/**
	 * Initialize snapshot in memory. It priotizes direct file path over environment based snapshot
	 * 
	 * @throws SwitcherSnapshotLoadException in case it was not possible to load snapshot automatically
	 */
	public void init() {
		final SwitcherProperties properties = SwitcherContext.getProperties();
		
		if (StringUtils.isNotBlank(properties.getSnapshotFile())) {
			this.domain = SnapshotLoader.loadSnapshot(properties.getSnapshotFile());
		} else if (StringUtils.isNotBlank(properties.getSnapshotLocation())) {
			try {
				this.domain = SnapshotLoader.loadSnapshot(
						properties.getSnapshotLocation(), properties.getEnvironment());
			} catch (FileNotFoundException e) {
				if (properties.isSnapshotAutoLoad()) {
					this.domain = this.initializeSnapshotFromAPI();
				} else {
					throw new SwitcherSnapshotLoadException(
							String.format("%s/%s.json", 
								properties.getSnapshotLocation(), 
								properties.getEnvironment()), e);
				}
			}
		}
	}
	
	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final CriteriaResponse response = ClientOfflineServiceFacade.getInstance().executeCriteria(switcher, this.domain);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("[Offline] response: %s", response));
		}
		
		return response;
	}
	
	
	@Override
	public boolean checkSnapshotVersion() {
		return super.checkSnapshotVersion(this.domain);
	}

	@Override
	public void updateSnapshot() {
		this.domain = super.initializeSnapshotFromAPI();
	}
	
	@Override
	public void notifyChange(final String snapshotFile) {
		
		final SwitcherProperties properties = SwitcherContext.getProperties();
		
		try {
			if (snapshotFile.equals(String.format("%s.json", properties.getEnvironment()))) {
				logger.debug("Updating domain");
				this.domain = SnapshotLoader.loadSnapshot(
						properties.getSnapshotLocation(), properties.getEnvironment());
			}
		} catch (SwitcherSnapshotLoadException | FileNotFoundException e) {
			logger.error(e);
		}
	}
	
	public Domain getDomain() {
		return domain;
	}

}
