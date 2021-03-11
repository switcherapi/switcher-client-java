package com.github.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.Switchers;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.model.Switcher;

class SwitcherOnlineUnavailableTest {
	
	@BeforeEach
	void setupContext() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnError_noConnection() {
		//given
		SwitcherContext.getProperties().setUrl("http://localhost:30");
		SwitcherContext.initializeClient();
		
		//test
		Switcher switcher = Switchers.getSwitcher(Switchers.ONLINE_KEY);
		assertThrows(SwitcherAPIConnectionException.class, () -> {
			switcher.isItOn();
		});
	}

}
