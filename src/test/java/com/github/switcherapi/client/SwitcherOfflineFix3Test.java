package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationException;
import com.github.switcherapi.client.exception.SwitcherInvalidOperationInputException;
import com.github.switcherapi.client.exception.SwitcherInvalidStrategyException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

public class SwitcherOfflineFix3Test {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeAll
	static void setupContext() {
		Switchers.getProperties().setSnapshotFile(null);
		Switchers.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		Switchers.getProperties().setEnvironment("snapshot_fixture3");
		Switchers.getProperties().setOfflineMode(true);
		Switchers.initializeClient();
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotStrategy() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE11);
		switcher.prepareEntry(new Entry("INVALID_NAME_FOR_VALIDATION", "Value"));
		
		assertThrows(SwitcherInvalidStrategyException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForNetwork() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE12);
		switcher.prepareEntry(new Entry(Entry.NETWORK, "10.0.0.1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForValue() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE13);
		switcher.prepareEntry(new Entry(Entry.VALUE, "Value"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForNumeric() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE18);
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidValuesForNumericValidation() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE19);
		switcher.prepareEntry(new Entry(Entry.NUMERIC, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForDate() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE14);
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForTime() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE15);
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}

	@Test
	public void offlineShouldReturnError_InvalidValuesForDate() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE16);
		switcher.prepareEntry(new Entry(Entry.DATE, "2019-12-10"));
		
		assertThrows(SwitcherInvalidOperationInputException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidValuesForTime() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE17);
		switcher.prepareEntry(new Entry(Entry.TIME, "12:00"));
		
		assertThrows(SwitcherInvalidOperationInputException.class, () -> {
			switcher.isItOn();
		});
	}
	
	@Test
	public void offlineShouldReturnError_InvalidSnapshotOperationForRegex() {
		Switcher switcher = Switchers.getSwitcher(Switchers.USECASE20);
		switcher.prepareEntry(new Entry(Entry.REGEX, "1"));
		
		assertThrows(SwitcherInvalidOperationException.class, () -> {
			switcher.isItOn();
		});
	}

}
