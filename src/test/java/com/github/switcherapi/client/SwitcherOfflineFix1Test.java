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
import com.github.switcherapi.client.exception.SwitcherNoInputReceivedException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

class SwitcherOfflineFix1Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setEnvironment("snapshot_fixture1");
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	@Test
	void offlineShouldReturnTrue() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE12);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_groupDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE21);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_strategyDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE71);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldNotReturn_keyNotFound() {
		Switcher switcher = Switchers.getSwitcher(Switchers.NOT_FOUND_KEY);
		assertThrows(SwitcherKeyNotFoundException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnTrue_dateValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedDateValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);
		switcher.checkDate("2019-12-11");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_dateValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_dateValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE32);
		Entry input = new Entry(Entry.DATE, "2019-12-09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_dateValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE32);
		Entry input = new Entry(Entry.DATE, "2019-12-12");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_dateValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019-12-11");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_dateValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019-12-13");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_dateValidationWrongFormat() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = new Entry(Entry.DATE, "2019/121/13");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidTimeFormat.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnTrue_valueValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE41);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedValueValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE41);
		switcher.checkValue("Value1");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_valueValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE41);
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_valueValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE42);
		Entry input = new Entry(Entry.VALUE, "Value5");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_valueValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE42);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_valueValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE43);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_valueValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE43);
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_valueValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE44);
		Entry input = new Entry(Entry.VALUE, "Value2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_valueValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE44);
		Entry input = new Entry(Entry.VALUE, "Value1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedNumericValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		switcher.checkNumeric("2");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_numericValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationDoesNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE82);
		Entry input = new Entry(Entry.NUMERIC, "4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_numericValidationDoesNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE82);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE83);
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_numeircValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE83);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE84);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_numericValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE84);
		Entry input = new Entry(Entry.NUMERIC, "1");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnException_invalidNumericInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = new Entry(Entry.NUMERIC, "INVALID_NUMBER");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidNumericFormat.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE85);
		Entry input = new Entry(Entry.NUMERIC, "0.99");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE86);
		Entry input = new Entry(Entry.NUMERIC, "1.09");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_numericValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE87);
		Entry input = new Entry(Entry.NUMERIC, "2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_timeValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE51);
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedTimeValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE51);
		switcher.checkTime("11:00");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_timeValidationGreater() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE51);
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_timeValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE52);
		Entry input = new Entry(Entry.TIME, "09:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_timeValidationLower() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE52);
		Entry input = new Entry(Entry.TIME, "11:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_timeValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE53);
		Entry input = new Entry(Entry.TIME, "13:00");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_timeValidationBetween() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE53);
		Entry input = new Entry(Entry.TIME, "18:00");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_timeValidationWrongFormat() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE53);
		Entry input = new Entry(Entry.TIME, "2019-12-10");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidTimeFormat.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnTrue_networkValidationExistCIDR() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE61);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.4");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedNetworkValidationExistCIDR() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE61);
		switcher.checkNetwork("10.0.0.4");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_networkValidationExistCIDR() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE61);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_networkValidationNotExistCIDR() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE62);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.8");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_networkValidationNotExistCIDR() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE62);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_networkValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE63);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.2");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_networkValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE63);
		Entry input = new Entry(Entry.NETWORK, "10.0.0.5");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_strategyRequiresInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE63);
		assertThrows(SwitcherNoInputReceivedException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidStrategyInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		switcher.prepareEntry(new Entry("INVALID_STRATEGY_NAME", "Value"));
		assertThrows(SwitcherNoInputReceivedException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnTrue_regexValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE91);
		Entry input = new Entry(Entry.REGEX, "USER_10");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_chainedRegexValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE91);
		switcher.checkRegex("USER_10");
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_regexValidationExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE91);
		Entry input = new Entry(Entry.REGEX, "USER_100");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_regexValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE92);
		Entry input = new Entry(Entry.REGEX, "user-100");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_regexValidationNotExist() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE92);
		Entry input = new Entry(Entry.REGEX, "user-10");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_regexValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE93);
		Entry input = new Entry(Entry.REGEX, "USER_10");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_regexValidationEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE93);
		Entry input = new Entry(Entry.REGEX, "user-10");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnTrue_regexValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE94);
		Entry input = new Entry(Entry.REGEX, "user-10");
		
		switcher.prepareEntry(input);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void offlineShouldReturnFalse_regexValidationNotEqual() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE94);
		Entry input = new Entry(Entry.REGEX, "USER_10");
		
		switcher.prepareEntry(input);
		assertFalse(switcher.isItOn());
	}

}
