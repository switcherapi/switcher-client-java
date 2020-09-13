package com.github.switcherapi.client;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.switcherapi.client.exception.SwitcherFactoryContextException;
import com.github.switcherapi.client.utils.SwitcherContextParam;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherFactoryTest {
	
	final String CONTEXT_ERROR = "Something went wrong: Context has errors - %s not found";
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void offlineShouldReturnException_contextNotInitialized() throws Exception {
		SwitcherFactory.getSwitcher("USECASE11");
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowError_noUrl() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.URL"));
			throw e;
		}
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowError_noApi() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(SwitcherContextParam.URL, "http://localhost:3000");
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.APIKEY"));
			throw e;
		}
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowError_noDomain() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(SwitcherContextParam.URL, "http://localhost:3000");
			properties.put(SwitcherContextParam.APIKEY, "API_KEY");
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.DOMAIN"));
			throw e;
		}
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowError_noComponent() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(SwitcherContextParam.URL, "http://localhost:3000");
			properties.put(SwitcherContextParam.APIKEY, "API_KEY");
			properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.COMPONENT"));
			throw e;
		}
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowErrorWhenAutoLoad_noLocation() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(SwitcherContextParam.URL, "http://localhost:3000");
			properties.put(SwitcherContextParam.APIKEY, "API_KEY");
			properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
			properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
			properties.put(SwitcherContextParam.SNAPSHOT_AUTO_LOAD, true);
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.SNAPSHOT_LOCATION"));
			throw e;
		}
	}
	
	@Test(expected = SwitcherFactoryContextException.class)
	public void shouldThrowErrorWhenSilentMode_noRetryTimer() throws Exception {
		try {
			Map<String, Object> properties = new HashMap<String, Object>();
			properties.put(SwitcherContextParam.URL, "http://localhost:3000");
			properties.put(SwitcherContextParam.APIKEY, "API_KEY");
			properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
			properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
			properties.put(SwitcherContextParam.SILENT_MODE, true);
			SwitcherFactory.buildContext(properties, false);
		} catch (Exception e) {
			assertEquals(e.getMessage(), String.format(CONTEXT_ERROR, "SwitcherContextParam.RETRY_AFTER"));
			throw e;
		}
	}

}
