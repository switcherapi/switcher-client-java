package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherInvalidNumericFormat;
import com.github.switcherapi.client.exception.SwitcherInvalidTimeFormat;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.fixture.Product;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherLocal1Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	private static final String PAYLOAD_FIXTURE = new Gson().toJson(Product.getFixture());
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture1")
				.local(true));
		
		SwitcherContext.initializeClient();
	}
	
	@Test
	void localShouldValidateContext() {
		assertEquals(SNAPSHOTS_LOCAL, SwitcherContext.contextStr(ContextKey.SNAPSHOT_LOCATION));
		assertEquals("fixture1", SwitcherContext.contextStr(ContextKey.ENVIRONMENT));
		assertTrue(SwitcherContext.contextBol(ContextKey.LOCAL_MODE));
	}
	
	@Test
	void localShouldReturnTrue() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11, true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE12);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse_groupDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE21);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void localShouldReturnTrue_strategyDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE71);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void localShouldNotReturn_keyNotFound() {
		Switcher switcher = Switchers.getSwitcher(Switchers.NOT_FOUND_KEY);
		assertThrows(SwitcherKeyNotFoundException.class, switcher::isItOn);
	}

	@Test
	void localShouldReturnFalse_nullEntryForStrategy() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE31);

		List<Entry> entry = null;
		assertFalse(switcher
				.prepareEntry(entry)
				.isItOn());
	}
	
	static Stream<Arguments> dateTestArguments() {
	    return Stream.of(
    		//GREATER
			Arguments.of(Switchers.USECASE31, "2019-12-11", Boolean.TRUE),
			Arguments.of(Switchers.USECASE31, "2019-12-09", Boolean.FALSE),
			//LOWER
			Arguments.of(Switchers.USECASE32, "2019-12-09", Boolean.TRUE),
			Arguments.of(Switchers.USECASE32, "2019-12-12", Boolean.FALSE),
			//BETWEEN
			Arguments.of(Switchers.USECASE33, "2019-12-11", Boolean.TRUE),
			Arguments.of(Switchers.USECASE33, "2019-12-13", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("dateTestArguments")
	void localShouldTest_dateValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.DATE, input);

		assertEquals(expected, switcher.prepareEntry(entry).isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("dateTestArguments")
	void localShouldTestChained_dateValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkDate(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse_dateValidationWrongFormat() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		Entry input = Entry.build(StrategyValidator.DATE, "2019/121/13");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidTimeFormat.class, switcher::isItOn);
	}
	
	static Stream<Arguments> valueTestArguments() {
	    return Stream.of(
    		//EXIST
			Arguments.of(Switchers.USECASE41, "Value1", Boolean.TRUE),
			Arguments.of(Switchers.USECASE41, "Value5", Boolean.FALSE),
			//NOT_EXIST
			Arguments.of(Switchers.USECASE42, "Value5", Boolean.TRUE),
			Arguments.of(Switchers.USECASE42, "Value1", Boolean.FALSE),
			//EQUAL
			Arguments.of(Switchers.USECASE43, "Value1", Boolean.TRUE),
			Arguments.of(Switchers.USECASE43, "Value2", Boolean.FALSE),
			//NOT_EQUAL
			Arguments.of(Switchers.USECASE44, "Value2", Boolean.TRUE),
			Arguments.of(Switchers.USECASE44, "Value1", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("valueTestArguments")
	void localShouldTest_valueValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.VALUE, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("valueTestArguments")
	void localShouldTestChained_valueValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkValue(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	static Stream<Arguments> numericTestArguments() {
	    return Stream.of(
    		//EXIST
			Arguments.of(Switchers.USECASE81, "2", Boolean.TRUE),
			Arguments.of(Switchers.USECASE81, "4", Boolean.FALSE),
			//NOT_EXIST
			Arguments.of(Switchers.USECASE82, "4", Boolean.TRUE),
			Arguments.of(Switchers.USECASE82, "2", Boolean.FALSE),
			//EQUAL
			Arguments.of(Switchers.USECASE83, "1", Boolean.TRUE),
			Arguments.of(Switchers.USECASE83, "2", Boolean.FALSE),
			//NOT_EQUAL
			Arguments.of(Switchers.USECASE84, "2", Boolean.TRUE),
			Arguments.of(Switchers.USECASE84, "1", Boolean.FALSE),
			//LOWER
			Arguments.of(Switchers.USECASE85, "0.99", Boolean.TRUE),
			//GREATER
			Arguments.of(Switchers.USECASE86, "1.09", Boolean.TRUE),
			//BETWEEN
			Arguments.of(Switchers.USECASE87, "2", Boolean.TRUE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("numericTestArguments")
	void localShouldTest_numericValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.NUMERIC, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("numericTestArguments")
	void localShouldTestChained_numericValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkNumeric(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	@Test
	void localShouldReturnException_invalidNumericInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE81);
		Entry input = Entry.build(StrategyValidator.NUMERIC, "INVALID_NUMBER");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidNumericFormat.class, switcher::isItOn);
	}
	
	static Stream<Arguments> timeTestArguments() {
	    return Stream.of(
    		//GREATER
			Arguments.of(Switchers.USECASE51, "11:00", Boolean.TRUE),
			Arguments.of(Switchers.USECASE51, "09:00", Boolean.FALSE),
			//LOWER
			Arguments.of(Switchers.USECASE52, "09:00", Boolean.TRUE),
			Arguments.of(Switchers.USECASE52, "11:00", Boolean.FALSE),
			//BETWEEN
			Arguments.of(Switchers.USECASE53, "13:00", Boolean.TRUE),
			Arguments.of(Switchers.USECASE53, "18:00", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("timeTestArguments")
	void localShouldTest_timeValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.TIME, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("timeTestArguments")
	void localShouldTestChained_timeValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkTime(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse_timeValidationWrongFormat() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE53);
		Entry input = Entry.build(StrategyValidator.TIME, "2019-12-10");
		
		switcher.prepareEntry(input);
		assertThrows(SwitcherInvalidTimeFormat.class, switcher::isItOn);
	}
	
	static Stream<Arguments> networkTestArguments() {
	    return Stream.of(
    		//EXIST - CIDR
			Arguments.of(Switchers.USECASE61, "10.0.0.4", Boolean.TRUE),
			Arguments.of(Switchers.USECASE61, "10.0.0.8", Boolean.FALSE),
			//NOT_EXIST - CIDR
			Arguments.of(Switchers.USECASE62, "10.0.0.8", Boolean.TRUE),
			Arguments.of(Switchers.USECASE62, "10.0.0.5", Boolean.FALSE),
			//EXIST
			Arguments.of(Switchers.USECASE63, "10.0.0.2", Boolean.TRUE),
			Arguments.of(Switchers.USECASE63, "10.0.0.5", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("networkTestArguments")
	void localShouldTest_networkValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.NETWORK, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("networkTestArguments")
	void localShouldTestChained_networkValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkNetwork(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse_strategyRequiresInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE63);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void localShouldReturnFalse_invalidStrategyInput() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE33);
		switcher.prepareEntry(Entry.build(StrategyValidator.INVALID, "Value"));
		assertFalse(switcher.isItOn());
	}
	
	static Stream<Arguments> regexTestArguments() {
	    return Stream.of(
    		//EXIST
			Arguments.of(Switchers.USECASE91, "USER_10", Boolean.TRUE),
			Arguments.of(Switchers.USECASE91, "USER_100", Boolean.FALSE),
			//NOT_EXIST
			Arguments.of(Switchers.USECASE92, "user-100", Boolean.TRUE),
			Arguments.of(Switchers.USECASE92, "user-10", Boolean.FALSE),
			//EQUAL
			Arguments.of(Switchers.USECASE93, "USER_10", Boolean.TRUE),
			Arguments.of(Switchers.USECASE93, "user-10", Boolean.FALSE),
			//NOT_EQUAL
			Arguments.of(Switchers.USECASE94, "user-10", Boolean.TRUE),
			Arguments.of(Switchers.USECASE94, "USER_10", Boolean.FALSE),
			//EXIST (ReDoS)
			Arguments.of(Switchers.USECASE95, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("regexTestArguments")
	void localShouldTest_regexValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.REGEX, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("regexTestArguments")
	void localShouldTestChained_regexValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkRegex(input);
		assertEquals(expected, switcher.isItOn());
	}
	
	static Stream<Arguments> payloadTestArguments() {
	    return Stream.of(
    		//HAS_ONE
			Arguments.of(Switchers.USECASE100, PAYLOAD_FIXTURE, Boolean.TRUE),
			Arguments.of(Switchers.USECASE101, PAYLOAD_FIXTURE, Boolean.FALSE),
			Arguments.of(Switchers.USECASE101, "INVALID_JSON", Boolean.FALSE),
			//HAS_ALL
			Arguments.of(Switchers.USECASE102, PAYLOAD_FIXTURE, Boolean.TRUE),
			Arguments.of(Switchers.USECASE102, "INVALID_JSON", Boolean.FALSE)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("payloadTestArguments")
	void localShouldTest_payloadValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		Entry entry = Entry.build(StrategyValidator.PAYLOAD, input);
		
		switcher.prepareEntry(entry);
		assertEquals(expected, switcher.isItOn());
	}
	
	@ParameterizedTest()
	@MethodSource("payloadTestArguments")
	void localShouldTestChained_payloadValidation(String useCaseKey, String input, boolean expected) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.checkPayload(input);
		assertEquals(expected, switcher.isItOn());
	}

}
