package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-05-10
 */
public class SwitcherSnapshotWriteException extends SwitcherException {

	public SwitcherSnapshotWriteException(final String location, final Exception e) {
		super(String.format("Unable to write the snapshot into %s", location), e);
	}
}
