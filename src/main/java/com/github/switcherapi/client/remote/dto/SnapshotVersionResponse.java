package com.github.switcherapi.client.remote.dto;

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

	@Override
	public String toString() {
		return "SnapshotVersionResponse{" +
				"status=" + status +
				'}';
	}
}
