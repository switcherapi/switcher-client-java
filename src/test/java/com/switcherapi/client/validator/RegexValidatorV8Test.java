package com.switcherapi.client.validator;

import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.EntryOperation;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.criteria.StrategyConfig;
import com.switcherapi.client.service.WorkerName;
import com.switcherapi.client.service.validators.RegexValidatorV8;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnJre(value = { JRE.JAVA_8 })
class RegexValidatorV8Test {

	private static final String EVIL_REGEX = "^(([a-z])+.)+[A-Z]([a-z])+$";
	private static final String EVIL_INPUT = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

	static Stream<Arguments> evilRegexTestArguments() {
		return Stream.of(
				Arguments.of(EntryOperation.EXIST, Boolean.FALSE),
				Arguments.of(EntryOperation.NOT_EXIST, Boolean.FALSE),
				Arguments.of(EntryOperation.EQUAL, Boolean.FALSE),
				Arguments.of(EntryOperation.NOT_EQUAL, Boolean.FALSE)
		);
	}

	@ParameterizedTest()
	@MethodSource("evilRegexTestArguments")
	void shouldFailEvilRegexInput(EntryOperation operation, boolean expected) {
		//given
		RegexValidatorV8 regexValidator = new RegexValidatorV8();
		StrategyConfig strategyConfig = givenStrategy(operation, Collections.singletonList(EVIL_REGEX));
		Entry entry = Entry.of(StrategyValidator.REGEX, EVIL_INPUT);

		//test
		boolean actual = assertTimeoutPreemptively(Duration.ofMillis(5000), () -> regexValidator.process(strategyConfig, entry));
		assertEquals(expected, actual);
	}

	@Test
	void shouldBlackListEvilInput_immediateReturnNextCall() {
		//given
		RegexValidatorV8 regexValidator = new RegexValidatorV8();
		StrategyConfig strategyConfig = givenStrategy(EntryOperation.EXIST, Collections.singletonList(EVIL_REGEX));
		Entry entry = Entry.of(StrategyValidator.REGEX, EVIL_INPUT);

		//test
		boolean result = assertTimeoutPreemptively(Duration.ofMillis(4000), () -> regexValidator.process(strategyConfig, entry));
		assertFalse(result);

		result = assertTimeoutPreemptively(Duration.ofMillis(100), () -> regexValidator.process(strategyConfig, entry));
		assertFalse(result);
	}

	@Test
	void shouldBlackListEvilInput_immediateReturnNextCall_similarInput() {
		//given
		RegexValidatorV8 regexValidator = new RegexValidatorV8();
		StrategyConfig strategyConfig = givenStrategy(EntryOperation.EXIST, Collections.singletonList(EVIL_REGEX));

		//test
		Entry entry1 = Entry.of(StrategyValidator.REGEX, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		boolean result = assertTimeoutPreemptively(Duration.ofMillis(4000), () -> regexValidator.process(strategyConfig, entry1));
		assertFalse(result);

		Entry entry2 = Entry.of(StrategyValidator.REGEX, "bbbbaaaaaaaaaaaaaaa");
		result = assertTimeoutPreemptively(Duration.ofMillis(100), () -> regexValidator.process(strategyConfig, entry2));
		assertFalse(result);
	}

	@Test
	void shouldFail_nullInput() {
		//given
		RegexValidatorV8 regexValidator = new RegexValidatorV8();
		StrategyConfig strategyConfig = givenStrategy(EntryOperation.EXIST, Collections.singletonList(EVIL_REGEX));
		Entry entry = Entry.of(StrategyValidator.REGEX, null);

		//test
		boolean result = regexValidator.process(strategyConfig, entry);
		assertFalse(result);
	}

	@Test
	void shouldCompleteWorkerThreadsAfterTimeout() {
		//given
		RegexValidatorV8 regexValidator = new RegexValidatorV8();
		StrategyConfig strategyConfig = givenStrategy(EntryOperation.EXIST, Collections.singletonList(EVIL_REGEX));
		Entry entry = Entry.of(StrategyValidator.REGEX, EVIL_INPUT);

		//test
		boolean result = regexValidator.process(strategyConfig, entry);
		assertFalse(result);
		assertWorkerNotExists();
	}

	private StrategyConfig givenStrategy(EntryOperation operation, List<String> values) {
		return new StrategyConfig(
				StrategyValidator.REGEX.toString(),
				operation.toString(),
				"Regex validator strategy for test",
				Boolean.TRUE,
				values.toArray(new String[0])
		);
	}

	private void assertWorkerNotExists() {
		assertFalse(Thread.getAllStackTraces().keySet().stream()
						.anyMatch(t -> t.getName().equals(WorkerName.REGEX_VALIDATOR_WORKER.toString())));
	}

}
