package com.github.switcherapi.client.exception;

/**
 * @author rogerio
 * @since 2020-05-13
 */
public class SwitcherSnapshotWatcherException extends SwitcherException {
	
	private static final long serialVersionUID = 4548138997211494541L;

	public SwitcherSnapshotWatcherException(final String message, final Exception ex) {
		
		super(String.format("Something went wrong: %s", message), ex);
	}

}
