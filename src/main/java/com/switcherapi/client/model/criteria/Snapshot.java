package com.switcherapi.client.model.criteria;

/**
 * 
 * @author Roger Floriano (petruki)
 * @since 2020--5-9 - v1.0.2
 */
public class Snapshot {

	private Domain domain;

	public Domain getDomain() {
		return domain != null ? domain : new Domain();
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
