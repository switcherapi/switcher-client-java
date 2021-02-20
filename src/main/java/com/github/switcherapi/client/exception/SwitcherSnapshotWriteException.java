package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-05-10
 */
public class SwitcherSnapshotWriteException extends SwitcherException {
	
	private static final long serialVersionUID = 4209372097236126363L;

	public SwitcherSnapshotWriteException(final String location, final Exception e) {
		
		super(String.format("Unable to write the snapshot into %s", location), e);
	}
}
