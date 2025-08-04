package com.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherInvalidStrategyException extends SwitcherException {

	public SwitcherInvalidStrategyException(final String strategyName) {
		super(String.format("Invalid strategy %s", strategyName), null);
	}

}
