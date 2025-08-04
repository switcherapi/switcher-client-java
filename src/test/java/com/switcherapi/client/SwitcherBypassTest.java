package com.switcherapi.client;

import com.switcherapi.client.model.StrategyValidator;
import com.switcherapi.client.model.Switcher;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.client.test.SwitcherBypass;
import com.switcherapi.client.test.SwitcherTest;
import com.switcherapi.client.test.SwitcherTestValue;
import com.switcherapi.client.test.SwitcherTestWhen;
import com.switcherapi.fixture.MetadataErrorSample;
import com.switcherapi.fixture.MetadataSample;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.switcherapi.Switchers.*;
import static com.switcherapi.client.SwitcherContext.getSwitcher;
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
		SwitcherBypass.getBypass().clear();
	}
	
	@Test
	void shouldReturnFalse_afterAssumingItsFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();
		
		//test
		SwitcherRequest switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());

		SwitcherBypass.assume(USECASE11, false);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterAssumingItsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		SwitcherRequest switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());

		SwitcherBypass.assume(USECASE111, true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterForgettingItWasFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();
		
		//test
		SwitcherRequest switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());

		SwitcherBypass.assume(USECASE11, false);
		assertFalse(switcher.isItOn());

		SwitcherBypass.forget(USECASE11);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnFalse_afterAssumingItsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		SwitcherRequest switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());

		SwitcherBypass.assume(USECASE111, true);
		assertTrue(switcher.isItOn());

		SwitcherBypass.forget(USECASE111);
		assertFalse(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111, result = false)
	void shouldReturnFalse_usingAnnotationAsFalse() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		SwitcherRequest switcher = getSwitcher(USECASE111);
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
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1");
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
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1");
		assertTrue(switcher.isItOn());

		switcher = getSwitcher(USECASE41).checkValue("Value2");
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE41, switchers =
		@SwitcherTestValue(key = USECASE41, when = {
			@SwitcherTestWhen(strategy = StrategyValidator.VALUE, input = "Value2")
		})
	)
	void shouldReturnTrue_usingMultipleSwitchersAnnotationWhenValueMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value2");
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
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1");
		assertFalse(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111)
	void shouldReturnTrue_usingAnnotationAsTrue() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		SwitcherRequest switcher = getSwitcher(USECASE111);
		assertTrue(switcher.isItOn());
	}

	@SwitcherTest(key = USECASE111)
	void shouldReturnSwitcherBypassedAsReason() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();

		//test
		SwitcherRequest switcher = getSwitcher(USECASE111);
		SwitcherResult switcherResult = switcher.submit();
		assertTrue(switcherResult.isItOn());
		assertEquals("Switcher bypassed", switcherResult.getReason());
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
		SwitcherRequest switcher = getSwitcher(USECASE111);
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
		SwitcherRequest switcher = getSwitcher(USECASE111);
		SwitcherResult switcherResult = switcher.submit();
		assertEquals("123", switcherResult.getMetadata(MetadataSample.class).getTransactionId());
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
		SwitcherRequest switcher = getSwitcher(USECASE111);
		SwitcherResult switcherResult = switcher.submit();
		assertEquals("123", switcherResult.getMetadata(MetadataSample.class).getTransactionId());

		switcher = getSwitcher(USECASE112);
		switcherResult = switcher.submit();
		assertEquals("321", switcherResult.getMetadata(MetadataErrorSample.class).getErrorId());
	}

	@Test
	void shouldReturnTrue_afterAssumingItsTrueWhenValueMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value1");

		SwitcherBypass.assume(USECASE41, true)
						.when(StrategyValidator.VALUE, "Value1");

		assertTrue(switcher.isItOn());
	}

	@Test
	void shouldReturnFalse_afterAssumingItsTrueWhenValueNotMatches() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE1));
		SwitcherContext.initializeClient();

		//test
		Switcher switcher = getSwitcher(USECASE41).checkValue("Value2");

		SwitcherBypass.assume(USECASE41, true)
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
		SwitcherRequest switcher = getSwitcher(USECASE111);

		// Using String.format
		if (switcher.isItOn()) {
			return String.format("Switcher key is %s", USECASE111);
		}

		// Using String concatenation
        return "Switcher key is " + USECASE111;
	}

}
