package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class SwitcherUtilsTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@BeforeEach
	@ClearEnvironmentVariable(key = "ENVIRONMENT")
	void reloadProperties() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnOk_localLocationFound() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.local(true));
		
		assertDoesNotThrow(SwitcherContext::initializeClient);
	}
	
	@Test
	void shouldReturnError_snapshotHasErrors() {
		SwitcherContext.configure(ContextBuilder.builder()
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("defect_default"));
		
		assertThrows(SwitcherSnapshotLoadException.class,
				SwitcherContext::initializeClient);
	}
	
	@Test
	void shouldReturnInvalidFormat() throws ParseException {
		Date date1 = DateUtils.parseDate("2019-12-10 10:00:00", "yyyy-MM-dd HH:mm:ss");
		assertThrows(Exception.class, () -> SwitcherUtils.addTimeDuration("1w", date1));
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

	@Test
	void shouldReturnInvalidMilliFormat() {
		assertThrows(Exception.class, () -> SwitcherUtils.getMillis("1h"));
	}

	/**
	 * 1. Time value
	 * 2. expected return
	 *
	 * @return Stream of testable arguments
	 */
	static Stream<Arguments> milliArguments() {
		return Stream.of(
				Arguments.of(".5s", 500L),
				Arguments.of("1s", 1000L),
				Arguments.of("1m", 60000L)
		);
	}

	@ParameterizedTest()
	@MethodSource("milliArguments")
	void shouldReturnMillis(String time, long expectedValue) {
		assertEquals(expectedValue, SwitcherUtils.getMillis(time));
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
			Arguments.of("${SNAPSHOT_LOCAL:}", ""),
			Arguments.of("${ENVIRONMENT}", "staging")
	    );
	}
	
	@ParameterizedTest()
	@MethodSource("envArguments")
	@SetEnvironmentVariable(key = "ENVIRONMENT", value = "staging")
	void shouldReadProperties(String property, String expectedValue) {
		//given
		Properties prop = new Properties();
		prop.setProperty(ContextKey.ENVIRONMENT.getParam(), property);
		
		//test
		final String value = SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop);
		assertEquals(expectedValue, value);
	}
	
	@Test
	@SetEnvironmentVariable(key = "ENVIRONMENT", value = "test")
	void shouldReadPropertyFromEnvironmentIgnoreDefault() {
		final String expected = "test";
		
		Properties prop = new Properties();
		prop.setProperty(ContextKey.ENVIRONMENT.getParam(), "${ENVIRONMENT:default}");
		
		//test
		final String actual = SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop);
		assertEquals(expected, actual);
	}
	
	@Test
	@SetEnvironmentVariable(key = "ENVIRONMENT", value = "")
	void shouldNotReadUnsetProperty() {
		//given
		Properties prop = new Properties();
		prop.setProperty(ContextKey.ENVIRONMENT.getParam(), "${ENVIRONMENT}");
		
		//test
		final String envParam = ContextKey.ENVIRONMENT.getParam();
		Exception ex = assertThrows(SwitcherContextException.class, () ->
			SwitcherUtils.resolveProperties(envParam, prop)
		);
		
		assertEquals("Something went wrong: Context has errors - Property ${ENVIRONMENT} not defined", ex.getMessage());
	}

}
