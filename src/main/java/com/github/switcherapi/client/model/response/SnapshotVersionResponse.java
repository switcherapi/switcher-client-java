package com.github.switcherapi.client.model.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author rogerio
 * @since 2020-05-13
 */
@JsonSerialize
public class SnapshotVersionResponse {
	
	private boolean status;

	public boolean isUpdated() {
		
		return status;
	}

	public void setStatus(boolean status) {
		
		this.status = status;
	}
	
}
