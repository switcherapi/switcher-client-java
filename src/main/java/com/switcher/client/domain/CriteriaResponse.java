package com.switcher.client.domain;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class CriteriaResponse {
	
	private boolean result;
	private String reason;
	
	public CriteriaResponse() {}
	
	public CriteriaResponse(final boolean result, final String reason) {
		
		this.result = result;
		this.reason = reason;
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

	@Override
	public String toString() {
		
		return "CriteriaResponse [result=" + result + ", reason=" + reason + "]";
	}
	
}
