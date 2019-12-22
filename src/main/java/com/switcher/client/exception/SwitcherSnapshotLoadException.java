package com.switcher.client.exception;

public class SwitcherSnapshotLoadException extends Exception {
	
	private static final long serialVersionUID = 5372308871473744595L;

	public SwitcherSnapshotLoadException(final String location, final Exception e) {
		
		super(String.format("Unable to load snapshot from %s", location), e);
	}
}
