package com.switcherapi.client.remote.dto;

import com.switcherapi.client.model.Entry;

import java.util.Arrays;

public class CriteriaInputRequest {

	private final Entry[] entry;

	public CriteriaInputRequest(final Entry[] entry) {
		this.entry = entry;
	}

	public Entry[] getEntry() {
		return this.entry;
	}

	@Override
	public String toString() {
		return "CriteriaInputRequest{" +
				"entry=" + Arrays.toString(entry) +
				'}';
	}
}
