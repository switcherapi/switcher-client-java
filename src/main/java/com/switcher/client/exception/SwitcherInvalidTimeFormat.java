package com.switcher.client.exception;

public class SwitcherInvalidTimeFormat extends Exception {
	
	private static final long serialVersionUID = 2747278362482612247L;

	public SwitcherInvalidTimeFormat(final String processName, final Exception e) {
		
		super(String.format("Something went wrong. Time/Date formatting was incorrect during the process %s", processName), e);
	}

}
