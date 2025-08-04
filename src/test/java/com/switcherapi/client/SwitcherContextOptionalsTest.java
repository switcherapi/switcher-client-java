package com.switcherapi.client;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.model.ContextKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SwitcherContextOptionalsTest {
	
	@BeforeEach
	void resetProperties() {
		SwitchersBase.loadProperties("switcherapi-optionals");
	}
	
	@Test
	void shouldLoadFromGivenPropertiesFile() {
		assertFalse(SwitchersBase.contextBol(ContextKey.LOCAL_MODE));
		assertEquals(1000, SwitchersBase.contextInt(ContextKey.TIMEOUT_MS));
		assertEquals(1, SwitchersBase.contextInt(ContextKey.POOL_CONNECTION_SIZE));
	}
}
