package com.github.switcherapi.client.model.criteria;

/**
 * 
 * @author Roger Floriano (petruki)
 * @since 2020--5-9 - v1.0.2
 */
public class Snapshot {

	private Criteria data;

	public Domain getDomain() {
		return data != null ? data.getDomain() : new Domain();
	}

	public Criteria getData() {
		return data;
	}

	public void setData(Criteria data) {
		this.data = data;
	}

}
