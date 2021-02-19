package com.github.switcherapi.client.model.response;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class CriteriaResponse {
	
	private boolean result;
	private String reason;
	private String switcherKey;
	
	public CriteriaResponse() {}
	
	public CriteriaResponse(final boolean result, final String reason, final String switcherKey) {
		this.result = result;
		this.reason = reason;
		this.switcherKey = switcherKey;
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

	@Override
	public String toString() {
		StringBuilder toString = new StringBuilder();
		toString.append("CriteriaResponse [");
		toString.append("switcherKey=").append(switcherKey);
		toString.append(", result=").append(result);
		if (reason != null)
			toString.append(", reason=").append(reason);
		toString.append(" ]");
		
		return toString.toString();
	}
	
}
