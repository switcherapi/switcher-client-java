package com.switcher.client.domain;

public class AuthRequest {
	
	private String domain;
	private String component;
	private String environment;
	
	public void setDomain(String domain) {
		
		this.domain = domain;
	}
	
	public void setComponent(String component) {
		
		this.component = component;
	}
	public void setEnvironment(String environment) {
		
		this.environment = environment;
	}

	@Override
	public String toString() {
		
		return "AuthRequest [domain=" + domain + ", component=" + component + ", environment=" + environment + "]";
	}

}
