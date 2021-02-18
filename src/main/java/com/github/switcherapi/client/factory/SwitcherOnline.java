package com.github.switcherapi.client.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.facade.ClientServiceFacade;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOnline extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOnline.class);
	
	private SwitcherOffline switcherOffline;
	
	public SwitcherOnline() {
		this.switcherOffline = new SwitcherOffline();
	}

	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		try {
			final CriteriaResponse response = ClientServiceFacade.getInstance().executeCriteria(switcher);
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Online] response: %s", response));
			}
			
			return response;
		} catch (final SwitcherAPIConnectionException e) {
			logger.error("Failed to execute criteria - {}", e.getMessage());
			return executeSilentCriteria(switcher, e);
		}
	}
	
	private CriteriaResponse executeSilentCriteria(final Switcher switcher, 
			final SwitcherAPIConnectionException e) {
		
		if (SwitcherContext.getProperties().isSilentMode()) {
			CriteriaResponse response = this.switcherOffline.executeCriteria(switcher);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Silent] response: %s", response));
			}
			
			return response;
		} else {
			throw e;
		}
	}

	@Override
	public boolean checkSnapshotVersion() {
		
		if (SwitcherContext.getProperties().getSnapshotLocation() != null) {
			return super.checkSnapshotVersion(this.switcherOffline.getDomain());
		}
		return Boolean.TRUE;
	}

	@Override
	public void updateSnapshot() {
		
		super.initializeSnapshotFromAPI();
	}
	
	@Override
	public void notifyChange(String snapshotFile) {
		
		this.switcherOffline.notifyChange(snapshotFile);
	}

}
