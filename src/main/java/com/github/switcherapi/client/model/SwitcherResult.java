package com.github.switcherapi.client.model;

import com.google.gson.Gson;

import java.util.*;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherResult {

	public static final String DEFAULT_REASON = "Default result";

	public static final String DEFAULT_SUCCESS = "Success";

	private boolean result;

	private String reason;

	private Object metadata;

	private String switcherKey;

	protected List<Entry> entry;

	protected Map<String, List<String>> entryWhen;

	public SwitcherResult() {
		entryWhen = new HashMap<>();
		entry = new ArrayList<>();
	}

	public SwitcherResult(String switcherKey, boolean result, String reason, List<Entry> entry) {
		this();
		this.result = result;
		this.reason = reason;
		this.switcherKey = switcherKey;
		this.entry = entry;
	}

	public SwitcherResult buildFromSwitcher(String switcherKey, List<Entry> entry) {
		this.switcherKey = switcherKey;
		this.entry = entry;

		if (Objects.nonNull(entry)) {
			for (Entry inputEntry : entry) {
				if (!isEntryMatching(inputEntry)) {
					return new SwitcherResult(switcherKey, !this.result, this.reason, entry);
				}
			}
		}

		return this;
	}

	private boolean isEntryMatching(Entry inputEntry) {
		return entryWhen.isEmpty() || entryWhen.containsKey(inputEntry.getStrategy()) &&
				entryWhen.get(inputEntry.getStrategy()).contains(inputEntry.getInput());
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

	public SwitcherResult when(StrategyValidator strategy, String input) {
		return when(strategy, List.of(input));
	}

	public SwitcherResult when(StrategyValidator strategy, List<String> inputs) {
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
		
		final SwitcherResult other = (SwitcherResult) obj;
		return Objects.equals(entry, other.entry) && 
				result == other.result && 
				Objects.equals(switcherKey, other.switcherKey);
	}

	@Override
	public String toString() {
		return "SwitcherResult [result=" + result +
				", reason=" + reason +
				", metadata=" + metadata +
				", switcherKey=" + switcherKey +
				", entry=" + entry + "]";
	}

}
