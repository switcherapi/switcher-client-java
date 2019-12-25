package com.switcher.client.factory;

import java.util.Map;

import org.apache.log4j.Logger;

import com.switcher.client.domain.CriteriaResponse;
import com.switcher.client.domain.Switcher;
import com.switcher.client.domain.criteria.Domain;
import com.switcher.client.exception.SwitcherAPIConnectionException;
import com.switcher.client.exception.SwitcherException;
import com.switcher.client.facade.ClientOfflineServiceFacade;
import com.switcher.client.facade.ClientServiceFacade;
import com.switcher.client.utils.SnapshotLoader;
import com.switcher.client.utils.SwitcherContextParam;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOnline implements SwitcherExecutor {
	
	private static final Logger logger = Logger.getLogger(SwitcherOnline.class);

	private Map<String, Object> properties;
	
	public SwitcherOnline(final Map<String, Object> properties) {
		
		this.init(properties);
	}
	
	public void init(final Map<String, Object> properties) {
		
		this.properties = properties;
	}

	@Override
	public boolean executeCriteria(final Switcher switcher) throws SwitcherException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		try {
			final CriteriaResponse response = ClientServiceFacade.getInstance().executeCriteria(this.properties, switcher);
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Online] response: %s", response));
			}
			
			return response.isItOn();
		} catch (final SwitcherAPIConnectionException e) {
			logger.error(e);
			return executeSilentCriteria(switcher, e);
		}
	}
	
	@Override
	public void updateContext(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	private boolean executeSilentCriteria(final Switcher switcher, final SwitcherAPIConnectionException e) throws SwitcherException {
		
		if (properties.containsKey(SwitcherContextParam.SILENT_MODE) &&
				(boolean) properties.get(SwitcherContextParam.SILENT_MODE)) {
			final Domain domain = SnapshotLoader.loadSnapshot((String) this.properties.get(SwitcherContextParam.SNAPSHOT_LOCATION));
			final CriteriaResponse response = ClientOfflineServiceFacade.getInstance().executeCriteria(switcher, domain);
			logger.debug(String.format("[Silent] response: %s", response));
			return response.isItOn();
		} else {
			throw e;
		}
	}

}
