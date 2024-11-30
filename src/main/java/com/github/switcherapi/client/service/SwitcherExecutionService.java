package com.github.switcherapi.client.service;

import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.model.SwitcherRequest;
import com.github.switcherapi.client.model.SwitcherResult;

/**
 * Switcher Execution Service encapsulates the criteria evaluation process for Switcher Requests.
 */
public class SwitcherExecutionService {

	private final SwitcherExecutor switcherExecutor;

	public SwitcherExecutionService(SwitcherExecutor switcherExecutor) {
		this.switcherExecutor = switcherExecutor;
	}

	public SwitcherResult executeCriteria(SwitcherRequest switcherRequest) {
		return switcherExecutor.executeCriteria(switcherRequest);
	}

}