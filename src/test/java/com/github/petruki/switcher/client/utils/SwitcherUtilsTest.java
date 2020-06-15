package com.github.petruki.switcher.client.utils;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import com.github.petruki.switcher.client.SwitcherFactory;
import com.github.petruki.switcher.client.exception.SwitcherSnapshotLoadException;
import com.github.petruki.switcher.client.model.Switcher;
import com.github.petruki.switcher.client.utils.SwitcherContextParam;
import com.github.petruki.switcher.client.utils.SwitcherUtils;

@PowerMockIgnore({"javax.management.*", "org.apache.log4j.*", "javax.xml.*", "javax.script.*"})
@RunWith(PowerMockRunner.class)
public class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	private Map<String, Object> properties;
	
	@Before
	public void setupContext() {

		properties = new HashMap<String, Object>();
		properties.put(SwitcherContextParam.URL, "http://localhost:3000");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$S2Wj/wG/Rfs3ij0xFbtgveDtyUAjML1/TOOhocDg5dhOaU73CEXfK");
		properties.put(SwitcherContextParam.DOMAIN, "switcher-domain");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-client");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
	}
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldReturnError_snapshotNotFound() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_FILE, SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
		switcher.isItOn();
	}
	
	@Test(expected = SwitcherSnapshotLoadException.class)
	public void shouldReturnError_envSnapshot_snapshotNotFound() throws Exception {
		properties.put(SwitcherContextParam.SNAPSHOT_LOCATION, SNAPSHOTS_LOCAL + "/UNKNOWN_FOLDER/");
		SwitcherFactory.buildContext(properties, true);
		
		Switcher switcher = SwitcherFactory.getSwitcher("USECASE11");
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
