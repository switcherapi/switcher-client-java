package com.github.switcherapi.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*", "javax.net.ssl.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherApiMockTest {
	
	private Map<String, Object> properties;
	private MockWebServer mockBackEnd;
	
	@Before
	public void setup() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        
        final String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        
        properties = new HashMap<>();
		properties.put(SwitcherContextParam.URL, baseUrl);
		properties.put(SwitcherContextParam.APIKEY, "API_KEY");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
    }
	
	@After
	public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }
	
	private MockResponse generateMockAuth(String token, int secondsAhead) 
			throws SwitcherInvalidDateTimeArgumentException {
		return new MockResponse()
				.setBody(String.format("{ \"token\": \"%s\", \"exp\": \"%s\" }", 
						token, SwitcherUtils.addTimeDuration(secondsAhead + "s", new Date()).getTime()/1000))
				.addHeader("Content-Type", "application/json");
	}
	
	private MockResponse generateCriteriaResponse(String result) {
		return new MockResponse()
			.setBody(String.format("{ \"result\": \"%s\" }", result))
			.addHeader("Content-Type", "application/json");
	}
	
	@Test
	public void shouldReturnTrue() throws SwitcherException {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("true"));
		
		//test
		SwitcherFactory.buildContext(properties, false);
		final Switcher switcher = SwitcherFactory.getSwitcher("ONLINE_KEY");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnFalse() throws SwitcherException {
		//mock /auth
		mockBackEnd.enqueue(generateMockAuth("token", 10));
		
		//mock /criteria
		mockBackEnd.enqueue(generateCriteriaResponse("false"));
		
		//test
		SwitcherFactory.buildContext(properties, false);
		final Switcher switcher = SwitcherFactory.getSwitcher("ONLINE_KEY");
		assertFalse(switcher.isItOn());
	}
	
	@Test(expected = SwitcherAPIConnectionException.class)
	public void shouldReturnError_noConnection() throws Exception {
		properties.put(SwitcherContextParam.URL, "http://localhost:30");
		SwitcherFactory.buildContext(properties, false);
		final Switcher switcher = SwitcherFactory.getSwitcher("ONLINE_KEY");
		switcher.isItOn();
	}

}
