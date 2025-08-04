package com.switcherapi;

import com.switcherapi.client.SwitcherContextBase;

public class SwitchersBaseNative extends SwitcherContextBase {

	public static final String USECASE11 = "USECASE11";

	@Override
	public void configureClient() {
		super.registerSwitcherKeys(USECASE11);
		super.configureClient();
	}

	public static SwitchersBaseNative buildSwitcherClientConfigMinimal(String url) {
		SwitchersBaseNative context = new SwitchersBaseNative();
		context.setUrl(url);
		context.setApikey("[API-KEY]");
		context.setDomain("domain");
		context.setComponent("component");
		return context;
	}

}
