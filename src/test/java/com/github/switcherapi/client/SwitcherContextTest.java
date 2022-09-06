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
				CONTEXT_ERROR, "URL not defined [add: switcher.url]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noApi() {
		Switchers.configure(ContextBuilder.builder().apiKey(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "API Key not defined [add: switcher.apikey]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noContext() {
		Switchers.configure(ContextBuilder.builder().contextLocation(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "Context class location not defined [add: switcher.context]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noDomain() {
		Switchers.configure(ContextBuilder.builder().domain(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "Domain not defined [add: switcher.domain]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noComponent() {
		Switchers.configure(ContextBuilder.builder().component(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "Component not defined [add: switcher.component]"), ex.getMessage());
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
				CONTEXT_ERROR, "Snapshot location not defined [add: switcher.snapshot.location]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenAutoLoad_noLocation() {
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(null)
				.snapshotAutoLoad(true));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "Snapshot location not defined [add: switcher.snapshot.location]"), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenSilentMode_noRetryTimer() {
		Switchers.configure(ContextBuilder.builder()
				.silentMode(true)
				.retryAfter(null));
		
		Exception ex = assertThrows(SwitcherContextException.class,
				Switchers::initializeClient);
		
		assertEquals(String.format(
				CONTEXT_ERROR, "Retry not defined [add: switcher.retry]"), ex.getMessage());
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

}
