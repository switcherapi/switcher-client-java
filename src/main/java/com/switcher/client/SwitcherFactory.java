package com.switcher.client;

import java.util.Map;

import org.apache.log4j.Logger;

import com.switcher.client.domain.Switcher;
import com.switcher.client.exception.SwitcherFactoryContextException;
import com.switcher.client.factory.SwitcherExecutor;
import com.switcher.client.factory.SwitcherOffline;
import com.switcher.client.factory.SwitcherOnline;
import com.switcher.client.utils.SwitcherContextParam;

public class SwitcherFactory {
	
	final static Logger logger = Logger.getLogger(SwitcherFactory.class);
	
	private static SwitcherExecutor instance;
	
	private SwitcherFactory() {}
	
	public static void buildContext(final Map<String, Object> properties, boolean offline) {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("properties: %s", properties));
			logger.debug(String.format("offline: %s", offline));
		}
		
		if (instance == null) {
			if (offline) {
				instance = new SwitcherOffline((String) properties.get(SwitcherContextParam.SNAPSHOT_LOCATION));
			} else {
				instance = new SwitcherOnline(properties);
			}
		} else {
			instance.updateContext(properties);
		}
	}
	
	public static Switcher getSwitcher(final String key) throws SwitcherFactoryContextException {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("key: %s", key));
		}
		
		if (instance == null) {
			throw new SwitcherFactoryContextException();
		}
		
		return new Switcher(key, instance);
	}

}
