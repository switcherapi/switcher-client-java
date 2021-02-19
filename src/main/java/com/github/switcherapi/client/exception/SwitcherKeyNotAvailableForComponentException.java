package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-07-30
 */
public class SwitcherKeyNotAvailableForComponentException extends SwitcherException {
	
	private static final long serialVersionUID = -2361118989316611317L;

	public SwitcherKeyNotAvailableForComponentException(final String component, final String switcherKey) {
		
		super(String.format("Component %s is not registered to %s", component, switcherKey), null);
	}

}
