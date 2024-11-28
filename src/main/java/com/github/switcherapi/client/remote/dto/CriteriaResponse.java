package com.github.switcherapi.client.remote.dto;

public class CriteriaResponse {

	private boolean result;

	private String reason;

	private Object metadata;

	private String switcherKey;

	public boolean getResult() {
		return result;
	}

	public String getReason() {
		return reason;
	}

	public Object getMetadata() {
		return metadata;
	}

	public String getSwitcherKey() {
		return switcherKey;
	}

	public void setSwitcherKey(String switcherKey) {
		this.switcherKey = switcherKey;
	}

	@Override
	public String toString() {
		return "CriteriaResponse{" +
				"result=" + result +
				", reason='" + reason + '\'' +
				", metadata=" + metadata +
				", switcherKey='" + switcherKey + '\'' +
				'}';
	}
}
