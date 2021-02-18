package com.github.switcherapi.client;

import static org.junit.Assert.assertFalse;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.model.Switcher;

public class SwitcherOfflineFix2Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		Switchers.getProperties().setSnapshotFile(null);
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setEnvironment("snapshot_fixture2");
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	@Test
	public void offlineShouldReturnFalse_domainDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE111);
		assertFalse(switcher.isItOn());
	}
	
}