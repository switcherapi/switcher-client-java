package com.switcherapi.client.utils;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.ContextBuilder;
import com.switcherapi.fixture.CountDownHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;

class SnapshotWatcherWorkerTest extends SnapshotTest {

	@BeforeAll
	static void setupContext() {
		SwitchersBase.configure(ContextBuilder.builder(true)
			.context(SwitchersBase.class.getName())
			.snapshotLocation(SNAPSHOTS_LOCAL)
			.environment(DEFAULT_ENV)
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
