package com.switcherapi.client.remote.dto;

import com.switcherapi.client.model.criteria.Snapshot;

public class SnapshotDataResponse {

	private Snapshot data;

	public Snapshot getData() {
		return data;
	}

	public void setData(Snapshot data) {
		this.data = data;
	}
}
