package com.github.switcherapi.playground;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherKey;

public class Features extends SwitcherContextBase {
	
	@SwitcherKey
	public static final String MY_SWITCHER = "MY_SWITCHER";

	@Override
	protected void configureClient() {
		configure(ContextBuilder.builder()
				.context(Features.class.getCanonicalName())
				.url("https://api.switcherapi.com")
				.apiKey("[API_KEY]")
				.component("switcher-playground")
				.domain("Playground")
				.local(true)
				.snapshotLocation("/snapshot/playground"));

		initializeClient();
	}
}
