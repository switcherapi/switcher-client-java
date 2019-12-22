package com.switcher.client.factory;

import java.util.Map;

import org.apache.log4j.Logger;

import com.switcher.client.domain.CriteriaResponse;
import com.switcher.client.domain.Switcher;
import com.switcher.client.domain.criteria.Domain;
import com.switcher.client.facade.ClientOfflineServiceFacade;
import com.switcher.client.utils.SnapshotLoader;
import com.switcher.client.utils.SwitcherContextParam;

public class SwitcherOffline implements SwitcherExecutor {
	
	private static final Logger logger = Logger.getLogger(SwitcherOffline.class);
	
	private String snapshotLocation;
	
	public SwitcherOffline(final String snapshotLocation) {
		
		this.init(snapshotLocation);
	}
	
	public void init(final String snapshotLocation) {
		
		this.snapshotLocation = snapshotLocation;
	}
	
	@Override
	public boolean executeCriteria(final Switcher switcher) throws Exception {
		
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

	public String getSnapshotLocation() {
		
		return this.snapshotLocation;
	}

}
