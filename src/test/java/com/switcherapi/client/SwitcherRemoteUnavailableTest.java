package com.switcherapi.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.switcherapi.Switchers;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.model.SwitcherRequest;

class SwitcherRemoteUnavailableTest {
	
	@BeforeEach
	void setupContext() {
		SwitcherContext.loadProperties();
	}
	
	@Test
	void shouldReturnError_noConnection() {
		//given
		SwitcherContext.configure(ContextBuilder.builder().url("http://localhost:30"));
		SwitcherContext.initializeClient();
		
		//test
		SwitcherRequest switcher = Switchers.getSwitcher(Switchers.REMOTE_KEY);
		assertThrows(SwitcherRemoteException.class, switcher::isItOn);
	}

}
