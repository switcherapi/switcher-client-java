package com.github.switcherapi.client;

import static com.github.switcherapi.Switchers.USECASE11;
import static com.github.switcherapi.Switchers.USECASE111;
import static com.github.switcherapi.client.SwitcherContext.getSwitcher;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import com.github.switcherapi.client.model.Switcher;

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
	
	@ParameterizedTest
	@SwitcherMock(key = USECASE111, result = false)
	void shouldReturnFalse_usingParametrizedTest() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
	}
	
	@ParameterizedTest
	@SwitcherMock(key = USECASE111, result = true)
	void shouldReturnTrue_usingParametrizedTest() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().snapshotLocation(SNAPSHOTS_LOCAL).environment(FIXTURE2));
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertTrue(switcher.isItOn());
	}

}
