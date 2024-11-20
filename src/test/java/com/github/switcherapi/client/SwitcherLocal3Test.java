package com.github.switcherapi.client;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.github.switcherapi.client.remote.ClientWS;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.service.SwitcherValidator;
import com.github.switcherapi.client.service.ValidatorService;
import com.github.switcherapi.client.service.local.ClientLocalService;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.service.local.SwitcherLocalService;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherLocal3Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture3")
				.local(true));
		
		SwitcherContext.initializeClient();
	}
	
	static Stream<Arguments> failTestArguments() {
	    return Stream.of(
			Arguments.of(Switchers.USECASE11, "INVALID_NAME_FOR_VALIDATION", "Value", SwitcherInvalidStrategyException.class),
			Arguments.of(Switchers.USECASE12, StrategyValidator.NETWORK.toString(), "10.0.0.1", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE13, StrategyValidator.VALUE.toString(), "Value", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE18, StrategyValidator.NUMERIC.toString(), "1", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE19, StrategyValidator.NUMERIC.toString(), "1", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE14, StrategyValidator.DATE.toString(), "2019-12-10", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE16, StrategyValidator.DATE.toString(), "2019-12-10", SwitcherInvalidOperationInputException.class),
			Arguments.of(Switchers.USECASE15, StrategyValidator.TIME.toString(), "12:00", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE17, StrategyValidator.TIME.toString(), "12:00", SwitcherInvalidOperationInputException.class),
			Arguments.of(Switchers.USECASE20, StrategyValidator.REGEX.toString(), "1", SwitcherInvalidOperationException.class),
			Arguments.of(Switchers.USECASE21, StrategyValidator.PAYLOAD.toString(), StringUtils.EMPTY, SwitcherInvalidOperationException.class)
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("failTestArguments")
	void localShouldReturnError(String useCaseKey, String strategyValidator,
			String input, Class<SwitcherException> error) {
		Switcher switcher = Switchers.getSwitcher(useCaseKey);
		switcher.prepareEntry(Entry.build(strategyValidator, input));
		
		assertThrows(error, switcher::isItOn);
	}
	
	@Test
	void localShouldCheckSwitchers() {
		//given
		Set<String> switchers = new HashSet<>();
		switchers.add(Switchers.USECASE20);
		switchers.add(Switchers.USECASE17);
		switchers.add(Switchers.USECASE16);

		ClientWS clientWS = ClientWSImpl.build();
		SwitcherValidator validatorService = new ValidatorService();
		SwitcherLocalService switcherLocal = new SwitcherLocalService(new ClientRemoteService(clientWS), new ClientLocalService(validatorService));
		switcherLocal.init();
		
		//test
		assertDoesNotThrow(() -> switcherLocal.checkSwitchers(switchers));
	}
	
	@Test
	void localShouldCheckSwitchers_notFound() {
		//given
		Set<String> notFound = new HashSet<>();
		notFound.add("NOT_FOUND_1");

		ClientWS clientWS = ClientWSImpl.build();
		SwitcherValidator validatorService = new ValidatorService();
		SwitcherLocalService switcherLocal = new SwitcherLocalService(new ClientRemoteService(clientWS), new ClientLocalService(validatorService));
		switcherLocal.init();
		
		//test
		Exception ex = assertThrows(SwitchersValidationException.class, () ->
				switcherLocal.checkSwitchers(notFound));
		
		assertEquals(String.format(
				"Something went wrong: Unable to load the following Switcher Key(s): %s", notFound), 
				ex.getMessage());
	}

	@Test
	void localShouldReturnTrue_defaultResult() {
		Switcher switcher = Switchers.getSwitcher(Switchers.NOT_FOUND_KEY);
		assertTrue(switcher.defaultResult(true).isItOn());
	}

}
