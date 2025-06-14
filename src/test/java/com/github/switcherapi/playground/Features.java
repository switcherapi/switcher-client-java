package com.github.switcherapi.playground;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherKey;

public class Features extends SwitcherContextBase {
	
	@SwitcherKey
	public static final String CLIENT_JAVA_FEATURE = "CLIENT_JAVA_FEATURE";

	@Override
	protected void configureClient() {
		configure(ContextBuilder.builder()
				.context(Features.class.getCanonicalName())
				.url("https://api.switcherapi.com")
				.apiKey(System.getenv("switcher.api.key"))
				.component(System.getenv("switcher.component"))
				.domain(System.getenv("switcher.domain"))
				.local(true)
				.snapshotLocation("./src/test/resources/snapshot/playground"));

		initializeClient();
	}
}
