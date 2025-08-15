package com.switcherapi.client;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import com.switcherapi.client.remote.ClientWS;
import com.switcherapi.client.remote.ClientWSImpl;
import com.switcherapi.client.service.SwitcherValidator;
import com.switcherapi.client.service.ValidatorService;
import com.switcherapi.client.service.local.ClientLocalService;
import com.switcherapi.client.service.remote.ClientRemoteService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.switcherapi.client.exception.SwitchersValidationException;
import com.switcherapi.client.model.Entry;
import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.service.local.SwitcherLocalService;

import static com.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.*;

class SwitcherLocal3Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";

	private static ExecutorService executorService;
	
	@BeforeAll
	static void setupContext() {
		executorService = Executors.newSingleThreadExecutor();
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture3")
				.local(true));
		
		SwitcherContext.initializeClient();
	}

	@AfterAll
	static void tearDown() {
		executorService.shutdown();
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
		SwitcherRequest switcher = Switchers.getSwitcher(useCaseKey);
		switcher.prepareEntry(Entry.of(strategyValidator, input));
		
		assertThrows(error, switcher::isItOn);
	}
	
	@Test
	void localShouldCheckSwitchers() {
		//given
		Set<String> switchers = new HashSet<>();
		switchers.add(Switchers.USECASE20);
		switchers.add(Switchers.USECASE17);
		switchers.add(Switchers.USECASE16);

		SwitcherProperties switcherProperties = SwitcherContext.getSwitcherProperties();
		ClientWS clientWS = ClientWSImpl.build(switcherProperties, executorService, DEFAULT_TIMEOUT);
		SwitcherValidator validatorService = new ValidatorService();
		SwitcherLocalService switcherLocal = new SwitcherLocalService(
				new ClientRemoteService(clientWS, switcherProperties),
				new ClientLocalService(validatorService), switcherProperties);
		switcherLocal.init();
		
		//test
		assertDoesNotThrow(() -> switcherLocal.checkSwitchers(switchers));
	}
	
	@Test
	void localShouldCheckSwitchers_notFound() {
		//given
		Set<String> notFound = new HashSet<>();
		notFound.add("NOT_FOUND_1");

		SwitcherProperties switcherProperties = SwitcherContext.getSwitcherProperties();
		ClientWS clientWS = ClientWSImpl.build(switcherProperties, executorService, DEFAULT_TIMEOUT);
		SwitcherValidator validatorService = new ValidatorService();
		SwitcherLocalService switcherLocal = new SwitcherLocalService(
				new ClientRemoteService(clientWS, switcherProperties),
				new ClientLocalService(validatorService), switcherProperties);
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
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.NOT_FOUND_KEY);
		assertTrue(switcher.defaultResult(true).isItOn());
	}

}
