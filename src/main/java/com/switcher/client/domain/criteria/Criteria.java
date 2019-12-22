package com.switcher.client.domain.criteria;

public class Criteria {
	
	private Domain domain;
	
	public Criteria() {}

	public Domain getDomain() {
		
		return domain;
	}

	public void setDomain(Domain domain) {
		
		this.domain = domain;
	}

	@Override
	public String toString() {
		
		return "Criteria [domain=" + domain + "]";
	}

}
