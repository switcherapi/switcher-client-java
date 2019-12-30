package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherNoInputReceivedException extends SwitcherException {
	
	private static final long serialVersionUID = -8414094976885969480L;

	public SwitcherNoInputReceivedException(final String strategyName) {
		
		super(String.format("Couln't find any input for the strategy %s", strategyName), null);
	}

}
