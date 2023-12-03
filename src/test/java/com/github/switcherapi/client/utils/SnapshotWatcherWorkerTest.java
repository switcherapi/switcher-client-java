package com.github.switcherapi.client.utils;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.service.WorkerName;
import com.github.switcherapi.fixture.CountDownHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SnapshotWatcherWorkerTest {

	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	@BeforeAll
	static void setupContext() {
		SwitchersBase.configure(ContextBuilder.builder()
			.contextLocation(SwitchersBase.class.getCanonicalName())
			.snapshotLocation(SNAPSHOTS_LOCAL)
			.environment("default")
			.silentMode(null)
			.regexTimeout(null)
			.local(true));

		SwitchersBase.initializeClient();
		SwitchersBase.stopWatchingSnapshot();
	}

	void assertWorker(boolean exists) {
		assertEquals(exists, Thread.getAllStackTraces().keySet().stream()
				.anyMatch(t -> t.getName().equals(WorkerName.SNAPSHOT_WATCH_WORKER.toString())));
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
