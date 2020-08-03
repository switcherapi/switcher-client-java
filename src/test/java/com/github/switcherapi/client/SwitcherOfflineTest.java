package com.github.switcherapi.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.exception.SwitcherNoInputReceivedException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.AuthRequest;
import com.github.switcherapi.client.utils.SwitcherContextParam;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherOfflineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
	}
	
	@Test
	public void offlineShouldReturnTrue() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_envSnapshot() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL);
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE12");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_envSnapshot() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL);
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE12");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_groupDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE21");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_domainDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture2.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE111");
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_strategyDisabled() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		// There is a disabled strategy requiring value validation.
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE71");
		assertTrue(switcher.isItOn());
	}
	
	@Test(expected = SwitcherKeyNotFoundException.class)
	public void offlineShouldNotReturn_keyNotFound() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("NOT_FOUND_KEY");
		switcher.isItOn();
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE31");
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE31");
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE32");
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE32");
		Entry input = new Entry(Entry.DATE, "2019-12-12");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019-12-13");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test(expected = SwitcherInvalidTimeFormat.class)
	public void offlineShouldReturnFalse_dateValidationWrongFormat() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		Entry input = new Entry(Entry.DATE, "2019/121/13");
		
		switcher.prepareEntry(input);
		switcher.isItOn();
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE41");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE41");
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE42");
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE42");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE43");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE43");
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE44");
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE44");
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE81");
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE81");
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationDoesNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE82");
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationDoesNotExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE82");
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE83");
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numeircValidationEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE83");
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE84");
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationNotEqual() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE84");
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test(expected = SwitcherInvalidNumericFormat.class)
	public void offlineShouldReturnException_invalidNumericInput() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE81");
		Entry input = new Entry(Entry.NUMERIC, "INVALID_NUMBER");
		
		switcher.prepareEntry(input);
		switcher.isItOn();
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE85");
		Entry input = new Entry(Entry.NUMERIC, "0.99");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE86");
		Entry input = new Entry(Entry.NUMERIC, "1.09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE87");
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationGreater() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationLower() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_timeValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "13:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_timeValidationBetween() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "18:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test(expected = SwitcherInvalidTimeFormat.class)
	public void offlineShouldReturnFalse_timeValidationWrongFormat() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
		Entry input = new Entry(Entry.TIME, "2019-12-10");
		
		switcher.prepareEntry(input);
		switcher.isItOn();
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationNotExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationNotExistCIDR() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_networkValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_networkValidationExist() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test(expected = SwitcherNoInputReceivedException.class)
	public void offlineShouldReturnFalse_strategyRequiresInput() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
		switcher.isItOn();
	}
	
	@Test
	public void shouldCreateAuthRequest() throws Exception {
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain((String) properties.get(SwitcherContextParam.DOMAIN));
		authRequest.setComponent((String) properties.get(SwitcherContextParam.COMPONENT));
		authRequest.setEnvironment((String) properties.get(SwitcherContextParam.ENVIRONMENT));
		
		assertNotNull(authRequest.toString());
		assertNotNull(authRequest.getComponent());
		assertNotNull(authRequest.getDomain());
		assertNotNull(authRequest.getEnvironment());
	}
	
	@Test(expected = SwitcherNoInputReceivedException.class)
	public void offlineShouldReturnError_InvalidStrategyInput() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
		switcher.prepareEntry(new Entry("INVALID_STRATEGY_NAME", "Value"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidStrategyException.class)
	public void offlineShouldReturnError_InvalidSnapshotStrategy() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		switcher.prepareEntry(new Entry("INVALID_NAME_FOR_VALIDATION", "Value"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidSnapshotOperationForNetwork() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE12");
		switcher.prepareEntry(new Entry(Entry.NETWORK, "10.0.0.1"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidSnapshotOperationForValue() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE13");
		switcher.prepareEntry(new Entry(Entry.VALUE, "Value"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidSnapshotOperationForNumeric() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE18");
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidValuesForNumericValidation() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE19");
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidSnapshotOperationForDate() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE14");
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationException.class)
	public void offlineShouldReturnError_InvalidSnapshotOperationForTime() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE15");
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		switcher.isItOn();
	}

	@Test(expected = SwitcherInvalidOperationInputException.class)
	public void offlineShouldReturnError_InvalidValuesForDate() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE16");
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherInvalidOperationInputException.class)
	public void offlineShouldReturnError_InvalidValuesForTime() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE17");
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		switcher.isItOn();
	}

}
