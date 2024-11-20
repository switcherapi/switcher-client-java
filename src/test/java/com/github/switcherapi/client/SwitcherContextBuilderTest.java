package com.github.switcherapi.client;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.github.switcherapi.SwitchersBase.USECASE11;
import static com.github.switcherapi.client.SwitcherContextBase.getSwitcher;
import static com.github.switcherapi.client.SwitcherContextValidator.ERR_LOCAL;
import static org.junit.jupiter.api.Assertions.*;

class SwitcherContextBuilderTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	@Test
	void shouldReturnSuccess() {
		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
				.contextLocation(SwitchersBase.class.getCanonicalName())
				.url("http://localhost:3000")
				.apiKey("API_KEY")
				.domain("switcher-domain")
				.component("switcher-client")
				.environment("default")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.local(true));
		
		SwitchersBase.initializeClient();
		
		//test
		Switcher switcher = getSwitcher(USECASE11);
		assertTrue(switcher.isItOn());
	}

	@Test
	void shouldReturnError_snapshotNotLoaded() {
		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
				.contextLocation(SwitchersBase.class.getCanonicalName())
				.url("http://localhost:3000")
				.apiKey("API_KEY")
				.domain("switcher-domain")
				.component("switcher-client")
				.environment("default")
				.snapshotLocation(null)
				.local(true));

		//test
		Exception exception = assertThrows(SwitcherContextException.class, SwitchersBase::initializeClient);
		assertEquals("Something went wrong: Context has errors - " + ERR_LOCAL, exception.getMessage());
	}
	
	@Test
	void shouldThrowError_wrongContextKeyTypeUsage() {
		//given
		SwitchersBase.configure(ContextBuilder.builder(true)
				.contextLocation(SwitchersBase.class.getCanonicalName())
				.domain("switcher-domain")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.local(true));

		SwitchersBase.initializeClient();

		assertThrows(SwitcherContextException.class, () -> SwitchersBase.contextBol(ContextKey.DOMAIN));
		assertThrows(SwitcherContextException.class, () -> SwitchersBase.contextStr(ContextKey.LOCAL_MODE));
	}

}
