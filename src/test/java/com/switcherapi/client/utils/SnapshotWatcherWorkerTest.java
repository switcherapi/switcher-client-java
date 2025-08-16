package com.switcherapi.client.utils;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.ContextBuilder;
import com.switcherapi.client.service.WorkerName;
import com.switcherapi.fixture.CountDownHelper;
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
		CountDownHelper.wait(1);
		assertWorkerUntil(true, 2);

		Thread.getAllStackTraces().keySet()
				.forEach(t -> {
					if (t.getName().equals(WorkerName.SNAPSHOT_WATCH_WORKER.toString())) {
						System.out.println("Thread: " + t.getName() + " - State: " + t.getState());
					}
				});

		SwitchersBase.stopWatchingSnapshot();
		CountDownHelper.wait(2);

		Thread.getAllStackTraces().keySet()
				.forEach(t -> {
					if (t.getName().equals(WorkerName.SNAPSHOT_WATCH_WORKER.toString())) {
						System.out.println("Thread: " + t.getName() + " - State: " + t.getState());
					}
				});

		assertWorkerUntil(false, 10);
	}

}
