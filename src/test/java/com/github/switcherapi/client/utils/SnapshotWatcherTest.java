package com.github.switcherapi.client.utils;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SnapshotWatcherTest {
	
	private static final Logger logger = LogManager.getLogger(SnapshotWatcherTest.class);
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() throws IOException {
		removeGeneratedFiles();
		generateFixture();
		
		SwitchersBase.configure(ContextBuilder.builder()
			.contextLocation(SwitchersBase.class.getCanonicalName())
			.environment("generated_watcher_default")
			.snapshotLocation(SNAPSHOTS_LOCAL)
			.offlineMode(true));
		
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
	
	static void removeGeneratedFiles() throws IOException {
		SwitchersBase.stopWatchingSnapshot();
		Files.deleteIfExists(Paths.get(SNAPSHOTS_LOCAL + "\\generated_watcher_default.json"));
	}
	
	static void generateFixture() {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_watcher.json"));
		mockedSnapshot.setData(criteria);
		
		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, "generated_watcher_default");
	}
	
	void changeFixture(boolean domainStatus) {
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/snapshot_watcher.json"));
		mockedSnapshot.setData(criteria);
		
		criteria.getDomain().setActivated(domainStatus);
		
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		writeFixture(gson.toJson(mockedSnapshot));
	}
	
	void writeFixture(String content) {
		try (
				final FileWriter fileWriter = new FileWriter(
						String.format("%s/%s.json", SNAPSHOTS_LOCAL, "generated_watcher_default"));

				final BufferedWriter bw = new BufferedWriter(fileWriter);
				final PrintWriter wr = new PrintWriter(bw)) {
			wr.write(content);
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	@Test
	void shouldNotReloadDomainAfterChangingSnapshot() throws InterruptedException {
		Switcher switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		
		//initial value is true
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(1, TimeUnit.SECONDS);
		
		SwitchersBase.stopWatchingSnapshot();
		this.changeFixture(false);
		
		waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);

		//snapshot file updated - does not change as the watcher has been terminated
		assertTrue(switcher.isItOn());
	}
	
	@Test
	void shouldReloadDomainAfterChangingSnapshot() throws InterruptedException {
		Switcher switcher = SwitchersBase.getSwitcher(SwitchersBase.USECASE11);
		
		//initial value is true
		assertTrue(switcher.isItOn());
		
		CountDownLatch waiter = new CountDownLatch(1);
		waiter.await(1, TimeUnit.SECONDS);
		
		this.changeFixture(false);
		
		waiter = new CountDownLatch(1);
		waiter.await(2, TimeUnit.SECONDS);

		//snapshot file updated - triggered domain reload
		assertFalse(switcher.isItOn());
	}

}
