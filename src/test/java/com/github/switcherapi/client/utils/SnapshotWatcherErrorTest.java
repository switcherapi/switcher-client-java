package com.github.switcherapi.client.utils;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.exception.SwitcherException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotWatcherErrorTest {

	@Test
	void shouldNotWatchSnapshotWhenRemote() {
		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
			.contextLocation(SwitchersBase.class.getCanonicalName())
			.url("https://api.switcherapi.com")
			.apiKey("[API_KEY]")
			.domain("Test")
			.component("switcher-test")
			.local(false));

		SwitchersBase.initializeClient();

		//test
		SwitcherException exception = assertThrows(SwitcherException.class, SwitchersBase::watchSnapshot);
		assertEquals("Something went wrong: Cannot watch snapshot when using remote", exception.getMessage());
	}
}
