package com.github.petruki.switcher.client.factory;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.petruki.switcher.client.exception.SwitcherAPIConnectionException;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.facade.ClientServiceFacade;
import com.github.petruki.switcher.client.model.CriteriaResponse;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherOnline implements SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherOnline.class);

	private Map<String, Object> properties;
	
	private SwitcherOffline switcherOffline;
	
	public SwitcherOnline(final Map<String, Object> properties) throws SwitcherException {
		
		this.init(properties);
	}
	
	public void init(final Map<String, Object> properties) throws SwitcherException {
		
		this.properties = properties;
		this.switcherOffline = new SwitcherOffline(this.properties);
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
			boolean response = this.switcherOffline.executeCriteria(switcher);
			logger.debug(String.format("[Silent] response: %s", response));
			return response;
		} else {
			throw e;
		}
	}

}
