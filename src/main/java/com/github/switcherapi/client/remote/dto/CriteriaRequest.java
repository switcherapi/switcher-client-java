package com.github.switcherapi.client.remote.dto;

import com.github.switcherapi.client.model.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CriteriaRequest {

	private String switcherKey;

	private List<Entry> entry;

	private boolean bypassMetric;

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

	public boolean isBypassMetric() {
		return bypassMetric;
	}

	public void setBypassMetric(boolean bypassMetric) {
		this.bypassMetric = bypassMetric;
	}

	/**
	 * This method builds up the request made by the client to reach the Switcher API.
	 *
	 * @return json input request
	 */
	public CriteriaInputRequest getInputRequest() {
		return new CriteriaInputRequest(
				Optional.ofNullable(this.entry)
						.orElseGet(ArrayList::new)
						.toArray(new Entry[0]));
	}

}
