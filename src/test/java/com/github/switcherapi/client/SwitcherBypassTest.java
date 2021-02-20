package com.github.switcherapi.client;

import static com.github.switcherapi.Switchers.USECASE11;
import static com.github.switcherapi.Switchers.USECASE111;
import static com.github.switcherapi.client.configuration.SwitcherContext.getSwitcher;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.github.switcherapi.client.model.Switcher;

class SwitcherBypassTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	@Test
	void shouldReturnFalse_afterAssumingItsFalse() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE11, false);
		assertFalse(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterAssumingItsTrue() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture2.json");
		SwitcherContext.initializeClient();
		
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE111, true);
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReturnTrue_afterForgettingItWasFalse() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture1.json");
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
	void shouldReturnFalse_afterAssumingItsTrue() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/snapshot_fixture2.json");
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE111);
		assertFalse(switcher.isItOn());
		
		SwitcherExecutor.assume(USECASE111, true);
		assertTrue(switcher.isItOn());
		
		SwitcherExecutor.forget(USECASE111);
		assertFalse(switcher.isItOn());
	}

}
