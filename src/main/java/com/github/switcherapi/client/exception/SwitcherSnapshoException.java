package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2022-06-12
 */
public class SwitcherSnapshoException extends SwitcherException {

	public SwitcherSnapshoException(final String operation) {
		super(String.format("Unable to execute %s", operation), null);
	}
}
