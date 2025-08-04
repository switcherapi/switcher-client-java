package com.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherInvalidTimeFormat extends SwitcherException {

	public SwitcherInvalidTimeFormat(final String processName, final Exception e) {
		super(String.format("Time/Date formatting was incorrect during the process %s", processName), e);
	}

}
