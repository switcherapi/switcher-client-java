package com.github.petruki.switcher.client.factory;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.domain.CriteriaResponse;
import com.github.petruki.switcher.client.domain.Switcher;
import com.github.petruki.switcher.client.domain.criteria.Domain;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.facade.ClientOfflineServiceFacade;
import com.github.petruki.switcher.client.utils.SnapshotLoader;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOffline implements SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOffline.class);
	
	private String snapshotLocation;
	
	public SwitcherOffline(final String snapshotLocation) {
		
		this.init(snapshotLocation);
	}
	
	public void init(final String snapshotLocation) {
		
		this.snapshotLocation = snapshotLocation;
	}
	
	@Override
	public boolean executeCriteria(final Switcher switcher) throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final Domain domain = SnapshotLoader.loadSnapshot(this.snapshotLocation);
		final CriteriaResponse response = ClientOfflineServiceFacade.getInstance().executeCriteria(switcher, domain);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("[Offline] response: %s", response));
		}
		
		return response.isItOn();
	}
	
	@Override
	public void updateContext(final Map<String, Object> properties) {
		this.snapshotLocation = (String) properties.get(SwitcherContextParam.SNAPSHOT_LOCATION);
	}

}
