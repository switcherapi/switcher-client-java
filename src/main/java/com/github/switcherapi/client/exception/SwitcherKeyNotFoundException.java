package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherKeyNotFoundException extends SwitcherException {

	public SwitcherKeyNotFoundException(final String key) {
		super(String.format("Unable to load a key %s", key), null);
	}

}
