package com.github.switcherapi.client.model.response;

import java.util.List;

import com.github.switcherapi.client.model.Entry;
import com.github.switcherapi.client.model.Switcher;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class CriteriaResponse {
	
	private boolean result;
	
	private String reason;
	
	private String switcherKey;
	
	protected List<Entry> entry;
	
	public CriteriaResponse() {}
	
	public CriteriaResponse(final boolean result, final String reason,
			final Switcher switcher) {
		this.result = result;
		this.reason = reason;
		this.switcherKey = switcher.getSwitcherKey();
		this.entry = switcher.getEntry();
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
	public String toString() {
		final StringBuilder toString = new StringBuilder();
		toString.append("CriteriaResponse [");
		toString.append("switcherKey=").append(switcherKey);
		toString.append(", result=").append(result);
		if (reason != null)
			toString.append(", reason=").append(reason);
		toString.append(" ]");
		
		return toString.toString();
	}
	
}
