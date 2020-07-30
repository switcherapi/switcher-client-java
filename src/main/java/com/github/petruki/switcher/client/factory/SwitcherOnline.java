package com.github.petruki.switcher.client.factory;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherAPIConnectionException;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.facade.ClientServiceFacade;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.model.response.CriteriaResponse;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOnline extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOnline.class);
	
	private SwitcherOffline switcherOffline;
	
	public SwitcherOnline(final Map<String, Object> properties) throws SwitcherException {
		
		this.init(properties);
	}
	
	@Override
	public void init(final Map<String, Object> properties) throws SwitcherException {
		
		this.properties = properties;
		this.switcherOffline = new SwitcherOffline(this.properties);
	}

	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		try {
			final CriteriaResponse response = ClientServiceFacade.getInstance().executeCriteria(this.properties, switcher);
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Online] response: %s", response));
			}
			
			return response;
		} catch (final SwitcherAPIConnectionException e) {
			logger.error(e);
			return executeSilentCriteria(switcher, e);
		}
	}
	
	private CriteriaResponse executeSilentCriteria(final Switcher switcher, final SwitcherAPIConnectionException e) 
			throws SwitcherException {
		
		if (super.isSilentMode()) {
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
	public boolean checkSnapshotVersion() throws SwitcherException {

		return Boolean.TRUE;
	}

	@Override
	public void updateSnapshot() throws SwitcherException {
		
		super.initializeSnapshotFromAPI();
	}
	
	@Override
	public void notifyChange(String snapshotFile) {
		
		this.switcherOffline.notifyChange(snapshotFile);
	}
	
	@Override
	public void updateContext(Map<String, Object> properties) {
		
		this.properties = properties;
	}

}
