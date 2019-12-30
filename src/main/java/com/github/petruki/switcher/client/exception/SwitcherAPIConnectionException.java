package com.github.petruki.switcher.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherAPIConnectionException extends SwitcherException {
	
	private static final long serialVersionUID = 9138522025066942280L;

	public SwitcherAPIConnectionException(final String url, final Exception e) {
		
		super(String.format("It was not possible to reach the Switcher-API on this endpoint: %s", url), e);
	}
	
	public SwitcherAPIConnectionException(final String url) {
		
		super(String.format("This action will redirect to the Silent Mode. The endpoint used was: %s", url), null);
	}

}
