package com.github.petruki.switcher.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.SwitcherFactory;
import com.github.petruki.switcher.client.domain.Switcher;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherBypassTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources/";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000/criteria");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
	}
	
	@Test
	public void shouldReturnFalse_afterAssumingItsFalse() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
		
		switcher.assume("USECASE11", false);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnTrue_afterAssumingItsTrue() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "snapshot_fixture2.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE111");
		assertFalse(switcher.isItOn());
		
		switcher.assume("USECASE111", true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnTrue_afterForgettingItWasFalse() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
		
		switcher.assume("USECASE11", false);
		assertFalse(switcher.isItOn());
		
		switcher.forget("USECASE11");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void shouldReturnFalse_afterAssumingItsTrue() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "snapshot_fixture2.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE111");
		assertFalse(switcher.isItOn());
		
		switcher.assume("USECASE111", true);
		assertTrue(switcher.isItOn());
		
		switcher.forget("USECASE111");
		assertFalse(switcher.isItOn());
	}

}
