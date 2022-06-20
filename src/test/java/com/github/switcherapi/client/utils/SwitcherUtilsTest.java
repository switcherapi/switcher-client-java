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
import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.model.SwitcherContextParam;

class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath().toString() + "/src/test/resources";
	
	@BeforeEach
	@ClearEnvironmentVariable(key = "ENVIRONMENT")
	void reloadProperties() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnError_snapshotNotFound() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json"));
		
		assertThrows(SwitcherSnapshotLoadException.class ,() -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_offlineSnapshotNotFound() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotFile(SNAPSHOTS_LOCAL + "/UNKWNOW_SNAPSHOT_FILE.json")
				.offlineMode(true));
		
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnOk_offlineLocationFound() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.offlineMode(true));
		
		assertDoesNotThrow(() -> SwitcherContext.initializeClient());
	}
	
	@Test
	void shouldReturnError_offlineLocationNotFound() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotFile(SNAPSHOTS_LOCAL + "/UNKNOWN_LOCATION")
				.offlineMode(true));
		
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_offlineNoLocationAndFileSpecified() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(null)
				.snapshotFile(null)
				.offlineMode(true));
		
		assertThrows(SwitcherContextException.class, () -> {
			SwitcherContext.initializeClient();
		});
	}
	
	@Test
	void shouldReturnError_snapshotHasErrors() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("defect_default"));
		
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
			Arguments.of("${PORT:8080}", "8080"),
			Arguments.of("${ENVIRONMENT}", "staging")
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("envArguments")
	@SetEnvironmentVariable(key = "ENVIRONMENT", value = "staging")
	@EnabledOnJre(value = { JRE.JAVA_8, JRE.JAVA_11 })
	void shouldReadProperties(String property, String expectedValue) {
		//given
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, property);
		
		//test
		final String value = SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		assertEquals(expectedValue, value);
	}
	
	@Test
	@SetEnvironmentVariable(key = "ENVIRONMENT", value = "test")
	@EnabledOnJre(value = { JRE.JAVA_8, JRE.JAVA_11 })
	void shouldReadPropertyFromEnvironmentIgnoreDefault() throws Exception {
		final String expected = "test";
		
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, "${ENVIRONMENT:default}");
		
		//test
		final String actual = SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		assertEquals(expected, actual);
	}
	
	@Test
	void shouldNotReadUnsetProperty() throws Exception {
		//given
		Properties prop = new Properties();
		prop.setProperty(SwitcherContextParam.ENVIRONMENT, "${ENVIRONMENT}");
		
		//test
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			SwitcherUtils.resolveProperties(SwitcherContextParam.ENVIRONMENT, prop);
		});
		
		assertEquals("Something went wrong: Context has errors - Property ${ENVIRONMENT} not defined", ex.getMessage());
	}

}
