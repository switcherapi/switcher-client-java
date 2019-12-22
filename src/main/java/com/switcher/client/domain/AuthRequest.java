package com.switcher.client.domain;

public class AuthRequest {
	
	private String domain;
	private String component;
	private String environment;
	
	public String getDomain() {
		
		return domain;
	}
	
	public void setDomain(String domain) {
		
		this.domain = domain;
	}
	
	public String getComponent() {
		
		return component;
	}
	
	public void setComponent(String component) {
		
		this.component = component;
	}
	
	public String getEnvironment() {
		
		return environment;
	}
	
	public void setEnvironment(String environment) {
		
		this.environment = environment;
	}

	@Override
	public String toString() {
		
		return "AuthRequest [domain=" + domain + ", component=" + component + ", environment=" + environment + "]";
	}

}
