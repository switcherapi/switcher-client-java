package com.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.switcherapi.Switchers;
import com.switcherapi.client.model.SwitcherRequest;

class SwitcherLocal2Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/snapshot";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("fixture2")
				.local(true));
		
		SwitcherContext.initializeClient();
	}
	
	@Test
	void localShouldReturnFalse_domainDisabled() {
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.USECASE111);
		assertFalse(switcher.isItOn());
	}
	
}
