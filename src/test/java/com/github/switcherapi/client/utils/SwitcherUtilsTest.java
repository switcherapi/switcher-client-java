package com.github.switcherapi.client.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeEach
	void reloadProperties() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnError_snapshotNotFound() {
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json");
		assertThrows(SwitcherSnapshotLoadException.class,() -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_snapshotHasErrors() {
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setEnvironment("defect_default");
		
		assertThrows(SwitcherSnapshotLoadException.class,() -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldAdd1second() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1s", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 10:00:01", dateString);
	}
	
	@Test
	void shouldAdd1minute() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1m", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 10:01:00", dateString);
	}
	
	@Test
	void shouldAdd1hour() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1h", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 11:00:00", dateString);
	}
	
	@Test
	void shouldAdd1day() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1d", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-11 10:00:00", dateString);
	}
	
	@Test
	void shouldReturnInvalidFormat() throws ParseException {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		assertThrows(Exception.class, () -> {
			SwitcherUtils.addTimeDuration("1w", date1);
		});
	}
	
	@Test
	void shouldReturnInputRequest() {
		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(Entry.DATE, "2019-12-10"));
		
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		switcher.prepareEntry(entries);
		assertTrue(SwitcherUtils.isJson(switcher.getInputRequest().toString()));
	}
	
	@Test
	void shouldReadProperties() {
		//given
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, "default");
		
		//test
		final String value = SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		assertEquals("default", value);
	}
	
	@Test
	void shouldReadEnviromentProperties() {
		//given
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, String.format("${%s}", "TESTME"));
		
		//test
		final String value = SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		assertEquals("${TESTME}", value);
	}

}
