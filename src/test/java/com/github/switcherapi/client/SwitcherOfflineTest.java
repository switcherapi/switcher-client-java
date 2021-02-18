package com.github.switcherapi.client;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

public class SwitcherOfflineTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	@Test
	public void offlineShouldReturnTrue() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE12);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_groupDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE21);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_strategyDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE71);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldNotReturn_keyNotFound() {
		Switcher switcher = Switchers.getSwitcher(Switchers.NOT_FOUND_KEY);
		assertThrows(SwitcherKeyNotFoundException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE32);
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE32);
		Entry input = new Entry(Entry.DATE, "2019-12-12");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_dateValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019-12-13");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_dateValidationWrongFormat() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019/121/13");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidTimeFormat.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE41);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE41);
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE42);
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE42);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE43);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE43);
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_valueValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE44);
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_valueValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE44);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationDoesNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE82);
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationDoesNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE82);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE83);
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numeircValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE83);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE84);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnFalse_numericValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE84);
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	public void offlineShouldReturnException_invalidNumericInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "INVALID_NUMBER");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidNumericFormat.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnTrue_numericValidationLower() throws Exception {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE85);
		Entry input = new Entry(Entry.NUMERIC, "0.99");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
//	
//	@Test
//	public void offlineShouldReturnTrue_numericValidationGreater() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE86");
//		Entry input = new Entry(Entry.NUMERIC, "1.09");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_numericValidationBetween() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE87");
//		Entry input = new Entry(Entry.NUMERIC, "2");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_timeValidationGreater() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
//		Entry input = new Entry(Entry.TIME, "11:00");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_timeValidationGreater() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE51");
//		Entry input = new Entry(Entry.TIME, "09:00");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_timeValidationLower() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
//		Entry input = new Entry(Entry.TIME, "09:00");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_timeValidationLower() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE52");
//		Entry input = new Entry(Entry.TIME, "11:00");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_timeValidationBetween() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
//		Entry input = new Entry(Entry.TIME, "13:00");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_timeValidationBetween() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
//		Entry input = new Entry(Entry.TIME, "18:00");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test(expected = SwitcherInvalidTimeFormat.class)
//	public void offlineShouldReturnFalse_timeValidationWrongFormat() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE53");
//		Entry input = new Entry(Entry.TIME, "2019-12-10");
//		
//		switcher.prepareEntry(input);
//		switcher.isItOn();
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_networkValidationExistCIDR() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.4");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_networkValidationExistCIDR() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE61");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_networkValidationNotExistCIDR() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_networkValidationNotExistCIDR() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE62");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_networkValidationExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.2");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_networkValidationExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
//		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test(expected = SwitcherNoInputReceivedException.class)
//	public void offlineShouldReturnFalse_strategyRequiresInput() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE63");
//		switcher.isItOn();
//	}
	
//	@Test
//	public void offlineShouldReturnFalse_domainDisabled() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture2.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE111");
//		assertFalse(switcher.isItOn());
//	}
//	
//	
//	@Test
//	public void shouldCreateAuthRequest() throws Exception {
//		final AuthRequest authRequest = new AuthRequest();
//		authRequest.setDomain((String) properties.get(SwitcherContextParam.DOMAIN));
//		authRequest.setComponent((String) properties.get(SwitcherContextParam.COMPONENT));
//		authRequest.setEnvironment((String) properties.get(SwitcherContextParam.ENVIRONMENT));
//		
//		assertNotNull(authRequest.toString());
//		assertNotNull(authRequest.getComponent());
//		assertNotNull(authRequest.getDomain());
//		assertNotNull(authRequest.getEnvironment());
//	}
//	
//	@Test(expected = SwitcherNoInputReceivedException.class)
//	public void offlineShouldReturnError_InvalidStrategyInput() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE33");
//		switcher.prepareEntry(new Entry("INVALID_STRATEGY_NAME", "Value"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidStrategyException.class)
//	public void offlineShouldReturnError_InvalidSnapshotStrategy() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
//		switcher.prepareEntry(new Entry("INVALID_NAME_FOR_VALIDATION", "Value"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForNetwork() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE12");
//		switcher.prepareEntry(new Entry(Entry.NETWORK, "10.0.0.1"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForValue() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE13");
//		switcher.prepareEntry(new Entry(Entry.VALUE, "Value"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForNumeric() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE18");
//		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidValuesForNumericValidation() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE19");
//		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForDate() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE14");
//		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForTime() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE15");
//		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
//		switcher.isItOn();
//	}
//
//	@Test(expected = SwitcherInvalidOperationInputException.class)
//	public void offlineShouldReturnError_InvalidValuesForDate() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE16");
//		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
//		switcher.isItOn();
//	}
//	
//	@Test(expected = SwitcherInvalidOperationInputException.class)
//	public void offlineShouldReturnError_InvalidValuesForTime() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE17");
//		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
//		switcher.isItOn();
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_regexValidationExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE91");
//		Entry input = new Entry(Entry.REGEX, "USER_10");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_regexValidationExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE91");
//		Entry input = new Entry(Entry.REGEX, "USER_100");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_regexValidationNotExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE92");
//		Entry input = new Entry(Entry.REGEX, "user-100");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_regexValidationNotExist() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE92");
//		Entry input = new Entry(Entry.REGEX, "user-10");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_regexValidationEqual() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE93");
//		Entry input = new Entry(Entry.REGEX, "USER_10");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_regexValidationEqual() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE93");
//		Entry input = new Entry(Entry.REGEX, "user-10");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnTrue_regexValidationNotEqual() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE94");
//		Entry input = new Entry(Entry.REGEX, "user-10");
//		
//		switcher.prepareEntry(input);
//		assertTrue(switcher.isItOn());
//	}
//	
//	@Test
//	public void offlineShouldReturnFalse_regexValidationNotEqual() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE94");
//		Entry input = new Entry(Entry.REGEX, "USER_10");
//		
//		switcher.prepareEntry(input);
//		assertFalse(switcher.isItOn());
//	}
//	
//	@Test(expected = SwitcherInvalidOperationException.class)
//	public void offlineShouldReturnError_InvalidSnapshotOperationForRegex() throws Exception {
//		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/snapshot_fixture3.json");
//		SwitcherFactory.buildContext(properties, true);
//		
//		Switcher switcher = SwitcherFactory.getSwitcher("USECASE20");
//		switcher.prepareEntry(new Entry(Entry.REGEX, "1"));
//		switcher.isItOn();
//	}

}
