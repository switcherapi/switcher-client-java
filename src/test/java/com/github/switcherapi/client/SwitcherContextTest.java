package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;

class SwitcherContextTest {
	
	final String CONTEXT_ERROR = "Something went wrong: Context has errors - %s not found";
	
	@BeforeEach
	void resetProperties() {
		Switchers.loadProperties();
	}
	
	@Test
	void shouldNotThrowError_noUrl() {
		Switchers.configure(ContextBuilder.builder().url(null));
		assertDoesNotThrow(() -> Switchers.initializeClient());
	}
	
	@Test
	void shouldThrowError_noApi() {
		Switchers.configure(ContextBuilder.builder().apiKey(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			Switchers.initializeClient();
		});
		
		assertEquals(String.format(
				CONTEXT_ERROR, "SwitcherContextParam.APIKEY"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noDomain() {
		Switchers.configure(ContextBuilder.builder().domain(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			Switchers.initializeClient();
		});
		
		assertEquals(String.format(
				CONTEXT_ERROR, "SwitcherContextParam.DOMAIN"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_noComponent() throws Exception {
		Switchers.configure(ContextBuilder.builder().component(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			Switchers.initializeClient();
		});
		
		assertEquals(String.format(
				CONTEXT_ERROR, "SwitcherContextParam.COMPONENT"), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenAutoLoad_noLocation() {
		Switchers.configure(ContextBuilder.builder()
				.snapshotLocation(null)
				.snapshotAutoLoad(true));
		
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			Switchers.initializeClient();
		});
		
		assertEquals(String.format(
				CONTEXT_ERROR, "SwitcherContextParam.SNAPSHOT_LOCATION"), ex.getMessage());
	}
	
	@Test
	void shouldThrowErrorWhenSilentMode_noRetryTimer() {
		Switchers.configure(ContextBuilder.builder()
				.silentMode(true)
				.retryAfter(null));
		
		Exception ex = assertThrows(SwitcherContextException.class, () -> {
			Switchers.initializeClient();
		});
		
		assertEquals(String.format(
				CONTEXT_ERROR, "SwitcherContextParam.RETRY_AFTER"), ex.getMessage());
	}
	
	@Test
	void shouldThrowError_invalidSwitcher() {
		Exception ex = assertThrows(SwitcherKeyNotFoundException.class, () -> {
			Switchers.getSwitcher("INVALID_SWITCHER");
		});
		
		assertEquals("Something went wrong: Unable to load a key INVALID_SWITCHER", ex.getMessage());
	}
	
	@Test
	void shouldThrowError_cannotInstantiateContext() {
		Exception ex = assertThrows(IllegalStateException.class, () -> {
			new Switchers();
		});
		
		assertEquals("Context class cannot be instantiated", ex.getMessage());
	}

}
