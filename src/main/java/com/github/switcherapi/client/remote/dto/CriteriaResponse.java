package com.github.switcherapi.client.remote.dto;

import com.github.switcherapi.client.model.Entry;

import java.util.List;

public class CriteriaResponse {

	private boolean result;

	private String reason;

	private Object metadata;

	private String switcherKey;

	protected List<Entry> entry;

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Object getMetadata() {
		return metadata;
	}

	public void setMetadata(Object metadata) {
		this.metadata = metadata;
	}

	public String getSwitcherKey() {
		return switcherKey;
	}

	public void setSwitcherKey(String switcherKey) {
		this.switcherKey = switcherKey;
	}

	public List<Entry> getEntry() {
		return entry;
	}

	public void setEntry(List<Entry> entry) {
		this.entry = entry;
	}

}
