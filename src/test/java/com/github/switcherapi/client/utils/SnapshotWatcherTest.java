package com.github.switcherapi.client.utils;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.fixture.CountDownHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotWatcherTest extends SnapshotTest {

	@BeforeAll
	static void setupContext() throws IOException {
		removeGeneratedFiles();
		generateFixture();
		
		SwitchersBase.configure(ContextBuilder.builder()
			.context(SwitchersBase.class.getCanonicalName())
			.environment("generated_watcher_default")
			.snapshotLocation(SNAPSHOTS_LOCAL)
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
		SwitchersBase.watchSnapshot();
	}
	
	@Test
	void shouldNotReloadDomainAfterChangingSnapshot() {
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		
		//initial value is true
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(1);
		
		SwitchersBase.stopWatchingSnapshot();
		this.changeFixture();

		CountDownHelper.wait(2);

		//snapshot file updated - does not change as the watcher has been terminated
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReloadDomainAfterChangingSnapshot() {
		SwitcherRequest switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		
		//initial value is true
		assertTrue(switcher.isItOn());

		CountDownHelper.wait(1);
		
		this.changeFixture();

		CountDownHelper.wait(2);

		//snapshot file updated - triggered domain reload
		assertFalse(switcher.isItOn());
	}

}
