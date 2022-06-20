package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.model.Switcher;

class SwitcherOfflineFix2Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("snapshot_fixture2")
				.offlineMode(true));
		
		SwitcherContext.initializeClient();
	}
	
	@Test
	void offlineShouldReturnFalse_domainDisabled() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE111);
		assertFalse(switcher.isItOn());
	}
	
}
