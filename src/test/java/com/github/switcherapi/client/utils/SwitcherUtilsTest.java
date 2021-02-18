package com.github.switcherapi.client.utils;

import static com.github.switcherapi.Switchers.USECASE11;
import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.configuration.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.model.Switcher;

public class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldReturnError_snapshotNotFound() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json");
		SwitcherContext.initializeClient();
		
		Switcher switcher = Switchers.getSwitcher(USECASE11);
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldReturnError_envSnapshot_snapshotNotFound() throws Exception {
		//given
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/UNKNOWN_FOLDER/");
		SwitcherContext.initializeClient();
		
		Switcher switcher = Switchers.getSwitcher(USECASE11);
		switcher.isItOn();
	}
	
	@Test
	public void shouldAdd1second() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1s", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 10:00:01", dateString);
	}
	
	@Test
	public void shouldAdd1minute() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1m", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 10:01:00", dateString);
	}
	
	@Test
	public void shouldAdd1hour() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1h", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-10 11:00:00", dateString);
	}
	
	@Test
	public void shouldAdd1day() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1d", date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals("2019-12-11 10:00:00", dateString);
	}
	
	@Test(expected = Exception.class)
	public void shouldReturnInvalidFormat() throws Exception {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration("1w", date1);
	}

}
