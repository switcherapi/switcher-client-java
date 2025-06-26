package com.github.switcherapi.client.service;

import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.client.model.SwitcherResult;

import static com.github.switcherapi.client.model.SwitcherResult.DEFAULT_REASON;
import static com.github.switcherapi.client.model.SwitcherResult.DEFAULT_SUCCESS;

public class SwitcherFactory {

	private SwitcherFactory() {}

	public static SwitcherResult buildFromDefault(SwitcherRequest switcher) {
		return new SwitcherResult(
				switcher.getSwitcherKey(),
				Boolean.parseBoolean(switcher.getDefaultResult()),
				DEFAULT_REASON, switcher.getEntry());
	}

	public static SwitcherResult buildResultDisabled(String reason, SwitcherRequest switcher) {
		return new SwitcherResult(switcher.getSwitcherKey(), Boolean.FALSE, reason, switcher.getEntry());
	}

	public static SwitcherResult buildResultEnabled(SwitcherRequest switcher) {
		return new SwitcherResult(switcher.getSwitcherKey(), Boolean.TRUE, DEFAULT_SUCCESS, switcher.getEntry());
	}

}
