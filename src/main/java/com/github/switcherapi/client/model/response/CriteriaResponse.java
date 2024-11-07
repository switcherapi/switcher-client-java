package com.github.switcherapi.client.model.response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.StrategyValidator;
import com.github.switcherapi.client.model.Switcher;
import com.google.gson.Gson;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class CriteriaResponse {

	private static final String DEFAULT_REASON = "Default result";

	private static final String DEFAULT_SUCCESS = "Success";

	private boolean result;

	private String reason;

	private Object metadata;

	private String switcherKey;

	protected List<Entry> entry;

	protected Map<String, List<String>> entryWhen;

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

		if (Objects.nonNull(entry)) {
			for (Entry entry : entry) {
				if (entryWhen.containsKey(entry.getStrategy()) && !entryWhen.get(entry.getStrategy()).contains(entry.getInput())) {
					return new CriteriaResponse(!this.result, this.reason, switcher);
				}
			}
		}

		return this;
	}

	public static CriteriaResponse buildFromDefault(Switcher switcher) {
		return new CriteriaResponse(Boolean.parseBoolean(switcher.getDefaultResult()), DEFAULT_REASON, switcher);
	}

	public static CriteriaResponse buildResultFail(String reason, Switcher switcher) {
		return new CriteriaResponse(Boolean.FALSE, reason, switcher);
	}

	public static CriteriaResponse buildResultSuccess(Switcher switcher) {
		return new CriteriaResponse(Boolean.TRUE, DEFAULT_SUCCESS, switcher);
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

	public CriteriaResponse when(StrategyValidator strategy, String input) {
		return when(strategy, List.of(input));
	}

	public CriteriaResponse when(StrategyValidator strategy, List<String> inputs) {
		if (entryWhen == null) {
			entryWhen = new HashMap<>();
		}

		entryWhen.put(strategy.toString(), inputs);
		return this;
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
		return "CriteriaResponse [result=" + result +
				", reason=" + reason +
				", metadata=" + metadata +
				", switcherKey=" + switcherKey +
				", entry=" + entry + "]";
	}

}
