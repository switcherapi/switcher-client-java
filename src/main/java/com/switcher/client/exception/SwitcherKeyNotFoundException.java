package com.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherKeyNotFoundException extends SwitcherException {
	
	private static final long serialVersionUID = -6415733343415447201L;

	public SwitcherKeyNotFoundException(final String key) {
		
		super(String.format("Unable to load a key %s", key), null);
	}

}
