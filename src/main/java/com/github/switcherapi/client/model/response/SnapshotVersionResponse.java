package com.github.switcherapi.client.model.response;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-05-13
 */
public class SnapshotVersionResponse {

	private boolean status;

	public boolean isUpdated() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

}
