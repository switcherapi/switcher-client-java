package com.github.switcherapi.client.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherSnapshotWatcherException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Criteria;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SnapshotWatcherTest {
	
	private static final Logger logger = LogManager.getLogger(SnapshotWatcherTest.class);
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "generated_watcher_default");
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL);
		properties.put(SwitcherContextParam.SNAPSHOT_AUTO_LOAD, true);
		
		this.removeFixture();
	}
	
	private void generateFixture() throws Exception {
		
		final Snapshot mockedSnapshot = new Snapshot();
		final Criteria criteria = new Criteria();
		criteria.setDomain(SnapshotLoader.loadSnapshot(SNAPSHOTS_LOCAL + "/default.json"));
		mockedSnapshot.setData(criteria);
		
		SnapshotLoader.saveSnapshot(mockedSnapshot, SNAPSHOTS_LOCAL, "generated_watcher_default");
	}
	
	/**
	 * Manually change the snapshot
	 * 
	 * @param domainStatus
	 * @throws Exception
	 */
	private void changeFixture(boolean domainStatus) throws Exception {
		
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

	private void removeFixture() {
		final File generatedFixture = new File(SNAPSHOTS_LOCAL + "/generated_watcher_default.json");
		
		if (generatedFixture.exists()) {
			generatedFixture.delete();
		}
	}
	
	@Test(expected = SwitcherSnapshotWatcherException.class)
	public void shouldReturnErrorWhenStopWatching() throws Exception {
		this.generateFixture();
		SwitcherFactory.buildContext(this.properties, true);
		
		Field field = PowerMockito.field(SwitcherUtils.class, "watcher");
		field.set(SwitcherUtils.class, null);
		
		SwitcherFactory.stopWatchingSnapshot();
	}
	
	@Test
	public void shouldReloadDomainAfterChangingSnapshot() throws Exception {
		this.generateFixture();
		SwitcherFactory.buildContext(this.properties, true);
		SwitcherFactory.watchSnapshot();
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		assertTrue(switcher.isItOn());
		
		this.changeFixture(false);
		
		// Not a Thread.sleep fan
		Awaitility.await().pollDelay(2, TimeUnit.SECONDS).until(() -> true);
		assertFalse(switcher.isItOn());
		
		SwitcherFactory.stopWatchingSnapshot();
	}

}
