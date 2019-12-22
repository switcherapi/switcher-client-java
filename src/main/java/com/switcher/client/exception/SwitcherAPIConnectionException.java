package com.switcher.client.exception;

public class SwitcherAPIConnectionException extends Exception {
	
	private static final long serialVersionUID = 9138522025066942280L;

	public SwitcherAPIConnectionException(final String url, final Exception e) {
		
		super(String.format("Something went wrong. It was not possible to reach the Switcher-API on this endpoint: %s", url), e);
	}
	
	public SwitcherAPIConnectionException(final String url) {
		
		super(String.format("Something went wrong. This action will redirect to the Silent Mode. The endpoint used was: %s", url));
	}

}
