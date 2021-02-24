package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherNoInputReceivedException extends SwitcherException {
	
	private static final long serialVersionUID = -8414094976885969480L;

	public SwitcherNoInputReceivedException(final String strategyName) {
		
		super(String.format("Could not find any input for the strategy %s", strategyName), null);
	}

}
