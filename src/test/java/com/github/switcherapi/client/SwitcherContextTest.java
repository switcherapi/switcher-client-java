package com.github.switcherapi.client;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SwitcherContextTest {
	
	final String CONTEXT_ERROR = "Something went wrong: Context has errors - %s";
	
	@BeforeEach
	void resetProperties() {
		Switchers.loadProperties();
	}
	
	@Test
	void shouldThrowError_noUrl() {
		Switchers.configure(ContextBuilder.builder().url(null));

		Exception ex = assertThrows(SwitcherContextException.class, Switchers::initializeClient);

		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_URL), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noApi() {
		Switchers.configure(ContextBuilder.builder().apiKey(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_API), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noContext() {
		Switchers.configure(ContextBuilder.builder().contextLocation(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_CONTEXT), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noDomain() {
		Switchers.configure(ContextBuilder.builder().domain(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_DOMAIN), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noComponent() {
		Switchers.configure(ContextBuilder.builder().component(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_COMPONENT), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenAutoLoad_noValidLocation() {
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources/invalid")
				.offlineMode(true)
				.snapshotAutoLoad(false));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_SNAPSHOT_LOCATION), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenAutoLoad_noLocation() {
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(null)
				.snapshotAutoLoad(true));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_SNAPSHOT_LOCATION), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenSilentMode_noRetryTimer() {
		Switchers.configure(ContextBuilder.builder()
				.silentMode(true)
				.retryAfter(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, SwitcherContextValidator.ERR_RETRY), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_invalidSwitcher() {
		Exception ex = assertThrows(SwitcherKeyNotFoundException.class, () ->
				Switchers.getSwitcher("INVALID_SWITCHER"));
		
		assertEquals("Something went wrong: Unable to load a key INVALID_SWITCHER", ex.getMessage());
	}
	
	@Test
	void shouldThrowError_cannotInstantiateContext() {
		Exception ex = assertThrows(IllegalStateException.class,
				Switchers::new);
		
		assertEquals("Context class cannot be instantiated", ex.getMessage());
	}

	@Test
	void shouldThrowError_invalidRegexTimeoutFormat() {
		Switchers.configure(ContextBuilder.builder()
				.regexTimeout("a"));

		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);

		assertEquals(String.format(
				CONTEXT_ERROR, String.format(SwitcherContextValidator.ERR_FORMAT, "switcher.regextimeout", "class java.lang.Integer")),
				ex.getMessage());
	}

	@Test
	void shouldThrowError_invalidSnapshotUpdateInterval() {
		Switchers.configure(ContextBuilder.builder()
				.snapshotAutoUpdateInterval("2s")
				.url("http://localhost")
				.snapshotLocation(null)
				.snapshotFile(null));

		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);

		assertEquals(String.format(
						CONTEXT_ERROR, SwitcherContextValidator.ERR_SNAPSHOT_AUTO_UPDATE),
				ex.getMessage());
	}

}
