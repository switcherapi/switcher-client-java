package com.github.switcherapi.playground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.configuration.SwitcherKey;
import com.github.switcherapi.client.model.Switcher;

public class LibPlayground extends SwitcherContext {
	
	final static Logger logger = LogManager.getLogger(LibPlayground.class);
	
	@SwitcherKey
	public static String MY_SWITCHER = "MY_SWITCHER";
	
	public LibPlayground() {
		getProperties().setContextLocation("com.github.switcherapi.playground.LibPlayground");
		getProperties().setApiKey("$2b$08$Hm77RoqpXb.1f7izs06uKendX.B1jjWqTZsfJAzYnFoRzJpEFQXEi");
		getProperties().setUrl("https://switcher-load-balance.herokuapp.com");
		getProperties().setDomain("Playground");
		getProperties().setComponent("switcher-playground");
		getProperties().setEnvironment("default");
		initializeClient();
		
		Switcher switcher = getSwitcher(MY_SWITCHER);
		switcher.setShowReason(true);
		logger.info(switcher.isItOn());
	}
	
	public static void main(String[] args) {
		new LibPlayground();
	}
}