package com.switcherapi.client.utils;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.ContextBuilder;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.fixture.CountDownHelper;
import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotWatcherContextTest extends SnapshotTest {
	
	@BeforeAll
	static void setupContext() throws IOException {
		removeGeneratedFiles();
		generateFixture();
		
		SwitchersBase.configure(ContextBuilder.builder(true)
			.context(SwitchersBase.class.getName())
			.environment("generated_watcher_default")
			.snapshotLocation(SNAPSHOTS_LOCAL)
			.snapshotWatcher(true)
			.local(true));
		
		SwitchersBase.initializeClient();
	}

	@AfterAll
	static void tearDown() {
		SwitchersBase.stopWatchingSnapshot();
	}
	
	@BeforeEach
	void prepareTest() {
		generateFixture();
	}

	@AfterEach
	void afterEach() {
		SwitchersBase.stopWatchingSnapshot();
		assertWorker(false);
	}
	
	@Test
	void shouldReloadDomainAfterChangingSnapshot() {
		//verify that the worker is running
		assertWorker(true);

		//given
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		
		//initial value is true
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(1);

		//when we change the fixture
		this.changeFixture();

		CountDownHelper.wait(2);

		//snapshot file updated - triggered domain reload
		assertFalse(switcher.isItOn());
	}

}
