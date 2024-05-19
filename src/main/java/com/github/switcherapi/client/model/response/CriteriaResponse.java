package com.github.switcherapi.client.model.response;

import java.util.List;
import java.util.Objects;

import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;
import com.google.gson.Gson;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class CriteriaResponse {

	private static final String DEFAULT_REASON = "Default result";

	private boolean result;

	private String reason;

	private Object metadata;

	private String switcherKey;

	protected List<Entry> entry;

	public CriteriaResponse() {
	}

	public CriteriaResponse(final boolean result, final String reason, final Switcher switcher) {
		this.result = result;
		this.reason = reason;
		this.switcherKey = switcher.getSwitcherKey();
		this.entry = switcher.getEntry();
	}

	public CriteriaResponse buildFromSwitcher(Switcher switcher) {
		this.switcherKey = switcher.getSwitcherKey();
		this.entry = switcher.getEntry();
		return this;
	}

	public static CriteriaResponse buildFromDefault(Switcher switcher) {
		return new CriteriaResponse(
				Boolean.parseBoolean(switcher.getDefaultResult()), DEFAULT_REASON, switcher);
	}

	public boolean isItOn() {
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

	public <T> T getMetadata(Class<T> clazz) {
		Gson gson = new Gson();
		return gson.fromJson(gson.toJson(metadata), clazz);
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

	@Override
	public int hashCode() {
		return Objects.hash(entry, result, switcherKey);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		final CriteriaResponse other = (CriteriaResponse) obj;
		return Objects.equals(entry, other.entry) && 
				result == other.result && 
				Objects.equals(switcherKey, other.switcherKey);
	}

	@Override
	public String toString() {
		final StringBuilder toString = new StringBuilder();
		toString.append("CriteriaResponse [result=").append(result)
				.append(", reason=").append(reason)
				.append(", metadata=").append(metadata)
				.append(", switcherKey=").append(switcherKey)
				.append(", entry=").append(entry).append("]");
		return toString.toString();
	}

}
