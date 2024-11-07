package com.github.switcherapi.client;

import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.test.SwitcherTest;
import com.github.switcherapi.client.test.SwitcherTestValue;
import com.github.switcherapi.client.test.SwitcherTestWhen;
import com.github.switcherapi.fixture.MetadataErrorSample;
import com.github.switcherapi.fixture.MetadataSample;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.github.switcherapi.Switchers.*;
import static com.github.switcherapi.client.SwitcherContext.getSwitcher;
import static org.junit.jupiter.api.Assertions.*;

class SwitcherBypassTest {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	private static final String FIXTURE1 = "fixture1";
	private static final String FIXTURE2 = "fixture2";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder().local(true));
	}
	
	@AfterAll
	static void resetMock() {
		SwitcherExecutor.getBypass().clear();
	}
	
	@Test
	void shouldReturnFalse_afterAssumingItsFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE11, false);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterAssumingItsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE111, true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterForgettingItWasFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE11, false);
		assertFalse(switcher.isItOn());
		
		SwitcherExecutor.forget(USECASE11);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse_afterAssumingItsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE111, true);
		assertTrue(switcher.isItOn());
		
		SwitcherExecutor.forget(USECASE111);
		assertFalse(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111, result = false)
	void shouldReturnFalse_usingAnnotationAsFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE41, when = {
			@SwitcherTestWhen(strategy = StrategyValidator.VALUE, input = "Value1")
	})
	void shouldReturnTrue_usingAnnotationAsTrueWhenValueMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1").build();
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE41, when = {
			@SwitcherTestWhen(strategy = StrategyValidator.VALUE, input = { "Value1", "Value2" })
	})
	void shouldReturnTrue_usingAnnotationAsTrueWhenValueSetMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1").build();
		assertTrue(switcher.isItOn());

		switcher = getSwitcher(USECASE41).checkValue("Value2").build();
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE41, switchers =
		@SwitcherTestValue(key = USECASE41, when = {
			@SwitcherTestWhen(strategy = StrategyValidator.VALUE, input = "Value1")
		})
	)
	void shouldReturnTrue_usingMultipleSwitchersAnnotationWhenValueMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1").build();
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE41, when = {
			@SwitcherTestWhen(strategy = StrategyValidator.VALUE, input = "Value2")
	})
	void shouldReturnFalse_usingAnnotationAsTrueWhenValueNotMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1").build();
		assertFalse(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111)
	void shouldReturnTrue_usingAnnotationAsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111)
	void shouldReturnSwitcherBypassedAsReason() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE111);
		CriteriaResponse criteriaResponse = switcher.submit();
		assertTrue(criteriaResponse.isItOn());
		assertEquals("Switcher bypassed", criteriaResponse.getReason());
	}

	@SwitcherTest(switchers = {
			@SwitcherTestValue(key = USECASE111),
			@SwitcherTestValue(key = USECASE112)
	})
	void shouldReturnTrue_usingMultipleSwitchersAnnotation() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertTrue(switcher.isItOn());

		switcher = getSwitcher(USECASE112);
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111, abTest = true)
	void shouldReturnSameResult_usingAbTest() {
		assertEquals("Switcher key is " + USECASE111, workBothWay());
	}

	@SwitcherTest(key = USECASE111, metadata = "{ \"transactionId\": \"123\" }")
	void shouldReturnWithMetadata() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE111);
		CriteriaResponse criteriaResponse = switcher.submit();
		assertEquals("123", criteriaResponse.getMetadata(MetadataSample.class).getTransactionId());
	}

	@SwitcherTest(switchers = {
			@SwitcherTestValue(key = USECASE111, metadata = "{ \"transactionId\": \"123\" }"),
			@SwitcherTestValue(key = USECASE112, metadata = "{ \"errorId\": \"321\" }")
	})
	void shouldReturnWithMetadata_usingMultipleSwitchersAnnotation() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE111);
		CriteriaResponse criteriaResponse = switcher.submit();
		assertEquals("123", criteriaResponse.getMetadata(MetadataSample.class).getTransactionId());

		switcher = getSwitcher(USECASE112);
		criteriaResponse = switcher.submit();
		assertEquals("321", criteriaResponse.getMetadata(MetadataErrorSample.class).getErrorId());
	}

	@Test
	void shouldReturnTrue_afterAssumingItsTrueWhenValueMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1").build();

		SwitcherExecutor.assume(USECASE41, true)
						.when(StrategyValidator.VALUE, "Value1");

		assertTrue(switcher.isItOn());
	}

	@Test
	void shouldReturnFalse_afterAssumingItsTrueWhenValueNotMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value2").build();

		SwitcherExecutor.assume(USECASE41, true)
				.when(StrategyValidator.VALUE, "Value1");

		assertFalse(switcher.isItOn());
		// check if no override was made during criteria submission
		assertFalse(switcher.isItOn());
	}

	/**
	 * Fake scenario to test both ways of building a String.
	 * It is used to AB Test behavior when the same result is expected.
	 */
	private String workBothWay() {
		Switcher switcher = getSwitcher(USECASE111);

		// Using String.format
		if (switcher.isItOn()) {
			return String.format("Switcher key is %s", USECASE111);
		}

		// Using String concatenation
        return "Switcher key is " + USECASE111;
	}

}
