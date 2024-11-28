package com.github.switcherapi.client.remote.dto;

import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CriteriaRequest {

	private String switcherKey;

	private List<Entry> entry;

	private boolean bypassMetric;

	public static CriteriaRequest build(final Switcher switcher) {
		final CriteriaRequest request = new CriteriaRequest();
		request.switcherKey = switcher.getSwitcherKey();
		request.entry = switcher.getEntry();
		request.bypassMetric = switcher.isBypassMetrics();
		return request;
	}

	public String getSwitcherKey() {
		return switcherKey;
	}

	public List<Entry> getEntry() {
		return entry;
	}

	public boolean isBypassMetric() {
		return bypassMetric;
	}

	/**
	 * This method builds up the request made by the client to reach the Switcher API.
	 *
	 * @return json input request
	 */
	public CriteriaRequest.GsonInputRequest getInputRequest() {
		return new CriteriaRequest.GsonInputRequest(
				Optional.ofNullable(this.entry)
						.orElseGet(ArrayList::new)
						.toArray(new Entry[0]));
	}

	public static class GsonInputRequest {

		private final Entry[] entry;

		public GsonInputRequest(final Entry[] entry) {
			this.entry = entry;
		}

		public Entry[] getEntry() {
			return this.entry;
		}
	}

}
