package com.github.switcherapi.client.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class SnapshotWatcherTest {
	
	private static final Logger logger = LogManager.getLogger(SnapshotWatcherTest.class);
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() throws IOException {
		removeGeneratedFiles();
		generateFixture();
		
		SwitcherContext.getProperties().setUrl("http://localhost:3000");
		SwitcherContext.getProperties().setEnvironment("generated_watcher_default");
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setSnapshotAutoLoad(true);
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	static void removeGeneratedFiles() throws IOException {
		SwitcherContext.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "\\generated_watcher_default.json"));
	}
	
	static void generateFixture() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, "generated_watcher_default");
	}
	
	/**
	 * Manually change the snapshot
	 */
	void changeFixture(boolean domainStatus) {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		criteria.getDomain().setActivated(domainStatus);
		
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try (
				final FileWriter fileWriter = new FileWriter(
						new File(String.format("%s/%s.json", SNAPSHOTS_LOCAL, "generated_watcher_default")));

				final BufferedWriter bw = new BufferedWriter(fileWriter);
				final PrintWriter wr = new PrintWriter(bw);
				) {
			wr.write(gson.toJson(mockedSnapshot));
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Test
	void shouldReloadDomainAfterChangingSnapshot() throws InterruptedException {
		generateFixture();
		SwitcherContext.watchSnapshot();
		
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(1, TimeUnit.SECONDS);
		
		this.changeFixture(false);
		
		waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);

		assertFalse(switcher.isItOn());
	}

}
