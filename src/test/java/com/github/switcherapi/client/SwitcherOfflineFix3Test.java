package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.internal.guava.Sets;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.service.local.SwitcherLocalExecutorService;

class SwitcherOfflineFix3Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		SwitcherContext.loadProperties();
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setEnvironment("snapshot_fixture3");
		SwitcherContext.getProperties().setOfflineMode(true);
		SwitcherContext.initializeClient();
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotStrategy() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		switcher.prepareEntry(new Entry("INVALID_NAME_FOR_VALIDATION", "Value"));
		
		assertThrows(SwitcherInvalidStrategyException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForNetwork() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE12);
		switcher.prepareEntry(new Entry(Entry.NETWORK, "10.0.0.1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForValue() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE13);
		switcher.prepareEntry(new Entry(Entry.VALUE, "Value"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForNumeric() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE18);
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidValuesForNumericValidation() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE19);
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForDate() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE14);
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForTime() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE15);
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}

	@Test
	void offlineShouldReturnError_InvalidValuesForDate() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE16);
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		
		assertThrows(SwitcherInvalidOperationInputException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidValuesForTime() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE17);
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		
		assertThrows(SwitcherInvalidOperationInputException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldReturnError_InvalidSnapshotOperationForRegex() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE20);
		switcher.prepareEntry(new Entry(Entry.REGEX, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	void offlineShouldCheckSwitchers() {
		//given
		Set<String> switchers = Sets.newHashSet();
		switchers.add(Switchers.USECASE20);
		switchers.add(Switchers.USECASE17);
		switchers.add(Switchers.USECASE16);
		
		SwitcherLocalExecutorService switcherOffline = new SwitcherLocalExecutorService();
		switcherOffline.init();
		
		//test
		assertDoesNotThrow(() -> switcherOffline.checkSwitchers(switchers));
	}
	
	@Test
	void offlineShouldCheckSwitchers_notFound() {
		//given
		Set<String> notFound = Sets.newHashSet();
		notFound.add("NOT_FOUND_1");
		
		SwitcherLocalExecutorService switcherOffline = new SwitcherLocalExecutorService();
		switcherOffline.init();
		
		//test
		Exception ex = assertThrows(SwitchersValidationException.class, () -> {
			switcherOffline.checkSwitchers(notFound);
		});
		
		assertEquals(String.format(
				"Something went wrong: Unable to load the following Switcher Key(s): %s", notFound), 
				ex.getMessage());
	}

}
