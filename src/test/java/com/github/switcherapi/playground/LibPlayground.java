package com.github.switcherapi.playground;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.SwitcherKey;
import com.github.switcherapi.client.model.Switcher;

public class LibPlayground extends SwitcherContext {
	
	final static Logger logger = LogManager.getLogger(LibPlayground.class);
	
	@SwitcherKey
	public static String MY_SWITCHER = "MY_SWITCHER";
	
	public LibPlayground() {
		getProperties().setContextLocation("com.github.switcherapi.playground.Features");
		getProperties().setApiKey("JDJiJDA4JEFweTZjSTR2bE9pUjNJOUYvRy9raC4vRS80Q2tzUnk1d3o1aXFmS2o5eWJmVW11cjR0ODNT");
		getProperties().setUrl("https://switcher-api.herokuapp.com");
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