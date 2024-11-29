package com.github.switcherapi.client.service;

import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.SwitcherResult;

import static com.github.switcherapi.client.model.SwitcherResult.DEFAULT_REASON;
import static com.github.switcherapi.client.model.SwitcherResult.DEFAULT_SUCCESS;

public class SwitcherFactory {

	private SwitcherFactory() {}

	public static SwitcherResult buildFromDefault(Switcher switcher) {
		return new SwitcherResult(
				switcher.getSwitcherKey(),
				Boolean.parseBoolean(switcher.getDefaultResult()),
				DEFAULT_REASON, switcher.getEntry());
	}

	public static SwitcherResult buildResultFail(String reason, Switcher switcher) {
		return new SwitcherResult(switcher.getSwitcherKey(), Boolean.FALSE, reason, switcher.getEntry());
	}

	public static SwitcherResult buildResultSuccess(Switcher switcher) {
		return new SwitcherResult(switcher.getSwitcherKey(), Boolean.TRUE, DEFAULT_SUCCESS, switcher.getEntry());
	}

}
