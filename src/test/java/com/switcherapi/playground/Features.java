package com.switcherapi.playground;

import com.switcherapi.client.ContextBuilder;
import com.switcherapi.client.SwitcherContextBase;
import com.switcherapi.client.SwitcherKey;

public class Features extends SwitcherContextBase {
	
	@SwitcherKey
	public static final String CLIENT_JAVA_FEATURE = "CLIENT_JAVA_FEATURE";

	@Override
	protected void configureClient() {
		configure(ContextBuilder.builder()
				.context(Features.class.getName())
				.url("http://localhost:3001")
				.apiKey(System.getenv("switcher.api.key"))
				.component(System.getenv("switcher.component"))
				.domain(System.getenv("switcher.domain"))
				.authAutoRefresh(true));

		initializeClient();
	}
}
