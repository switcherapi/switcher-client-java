package com.github.switcherapi.client.exception;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class SwitcherInvalidTimeFormat extends SwitcherException {
	
	private static final long serialVersionUID = 2747278362482612247L;

	public SwitcherInvalidTimeFormat(final String processName, final Exception e) {
		
		super(String.format("Time/Date formatting was incorrect during the process %s", processName), e);
	}

}
