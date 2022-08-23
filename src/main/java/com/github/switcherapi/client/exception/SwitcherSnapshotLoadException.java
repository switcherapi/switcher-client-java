package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherSnapshotLoadException extends SwitcherException {

	public SwitcherSnapshotLoadException(final String location, final Exception e) {
		super(String.format("Unable to load the snapshot from %s", location), e);
	}
}
