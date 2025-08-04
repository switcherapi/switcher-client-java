package com.switcherapi.client.utils;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.criteria.Data;
import com.switcherapi.client.model.criteria.Domain;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.service.WorkerName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class SnapshotTest {

	private static final Logger logger = LoggerFactory.getLogger(SnapshotTest.class);

	protected static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	protected static void removeGeneratedFiles() throws IOException {
		SwitchersBase.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "\\generated_watcher_default.json"));
	}

	protected static void generateFixture() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Data data = new Data();
		data.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_watcher.json"));
		mockedSnapshot.setData(data);

		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, "generated_watcher_default");
	}

	protected void changeFixture() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Data data = new Data();
		data.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_watcher.json"));
		mockedSnapshot.setData(data);

		data.setDomain(new Domain(
				data.getDomain().getName(),
				data.getDomain().getDescription(),
				!data.getDomain().isActivated(),
				data.getDomain().getVersion(),
				data.getDomain().getGroup()));

		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		writeFixture(gson.toJson(mockedSnapshot));
	}

	protected void writeFixture(String content) {
		try (
				final FileWriter fileWriter = new FileWriter(
						String.format("%s/%s.json", SNAPSHOTS_LOCAL, "generated_watcher_default"));

				final BufferedWriter bw = new BufferedWriter(fileWriter);
				final PrintWriter wr = new PrintWriter(bw)) {
			wr.write(content);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	protected void assertWorker(boolean exists) {
		assertEquals(exists, Thread.getAllStackTraces().keySet().stream()
				.anyMatch(t -> t.getName().equals(WorkerName.SNAPSHOT_WATCH_WORKER.toString())));
	}
}
