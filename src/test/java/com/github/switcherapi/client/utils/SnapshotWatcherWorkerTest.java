package com.github.switcherapi.client.utils;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.fixture.CountDownHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SnapshotWatcherWorkerTest extends SnapshotTest {

	@BeforeAll
	static void setupContext() {
		SwitchersBase.configure(ContextBuilder.builder(true)
			.context(SwitchersBase.class.getCanonicalName())
			.snapshotLocation(SNAPSHOTS_LOCAL)
			.environment("default")
			.local(true));

		SwitchersBase.initializeClient();
	}

	@Test
	void shouldStartAndKillWorker() {
		SwitchersBase.watchSnapshot();
		assertWorker(true);

		SwitchersBase.stopWatchingSnapshot();
		CountDownHelper.wait(2);

		assertWorker(false);
	}

}
