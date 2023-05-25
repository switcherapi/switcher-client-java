package com.github.switcherapi.client.exception;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherRemoteException extends SwitcherException {

	public SwitcherRemoteException(final String url, final Exception e) {
		super(String.format("It was not possible to reach the Switcher-API on this endpoint: %s", url), e);
	}

	public SwitcherRemoteException(final String url, int status) {
		super(String.format("It was not possible to reach the Switcher-API on this endpoint: %s - status: %s", url, status), null);
	}

	public SwitcherRemoteException(final String url) {
		super(String.format("It was not possible to reach the Switcher-API on this endpoint: %s", url), null);
	}

}
