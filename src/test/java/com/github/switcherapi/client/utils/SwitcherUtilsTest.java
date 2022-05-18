package com.github.switcherapi.client.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;

class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeEach
	void reloadProperties() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnError_snapshotNotFound() {
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json");
		assertThrows(SwitcherSnapshotLoadException.class ,() -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_offlineSnapshotNotFound() {
		SwitcherContext.getProperties().setSnapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json");
		SwitcherContext.getProperties().setOfflineMode(true);
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnOk_offlineLocationFound() {
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setOfflineMode(true);
		assertDoesNotThrow(() -> SwitcherContext.initializeClient());
	}
	
	@Test
	void shouldReturnError_offlineLocationNotFound() {
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL + "/UNKNOWN_LOCATION");
		SwitcherContext.getProperties().setOfflineMode(true);
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_offlineNoLocationAndFileSpecified() {
		SwitcherContext.getProperties().setSnapshotLocation(null);
		SwitcherContext.getProperties().setSnapshotFile(null);
		SwitcherContext.getProperties().setOfflineMode(true);
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_snapshotHasErrors() {
		SwitcherContext.getProperties().setSnapshotLocation(SNAPSHOTS_LOCAL);
		SwitcherContext.getProperties().setEnvironment("defect_default");
		
		assertThrows(SwitcherSnapshotLoadException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnInvalidFormat() throws ParseException {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		assertThrows(Exception.class, () -> {
			SwitcherUtils.addTimeDuration("1w", date1);
		});
	}
	
	/**
	 * 1. Value and time unit
	 * 2. expected return
	 * 
	 * @return Stream of testable arguments
	 */
	static Stream<Arguments> timeArguments() {
	    return Stream.of(
			Arguments.of("1s", "2019-12-10 10:00:01"),
			Arguments.of("1m", "2019-12-10 10:01:00"),
			Arguments.of("1h", "2019-12-10 11:00:00"),
			Arguments.of("1d", "2019-12-11 10:00:00")
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("timeArguments")
	void shouldAddTime(String time, String expectedValue) throws ParseException {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		date1 = SwitcherUtils.addTimeDuration(time, date1);
		String dateString = DateFormatUtils.format(date1, "yyyy-MM-dd HH:mm:ss");
		assertEquals(expectedValue, dateString);
	}
	
	/**
	 * 1. property value or variable
	 * 2. expected return
	 * 
	 * @return Stream of testable arguments
	 */
	static Stream<Arguments> envArguments() {
	    return Stream.of(
			Arguments.of("default", "default"),
			Arguments.of("${PORT}", "${PORT}"),
			Arguments.of("${PORT:8080}", "8080")
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("envArguments")
	void shouldReadProperties(String property, String expectedValue) {
		//given
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, property);
		
		//test
		final String value = SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		assertEquals(expectedValue, value);
	}

}
