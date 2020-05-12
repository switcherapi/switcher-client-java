package com.github.petruki.switcher.client.model.criteria;

/**
 * 
 * @author rogerio
 * @since 2020--5-9 - v1.0.2
 */
public class Snapshot {
	
	private Criteria data;
	
	public Domain getDomain() {
		
		return data.getDomain();
	}

	public Criteria getData() {
		
		return data;
	}

	public void setData(Criteria data) {
		
		this.data = data;
	}

}
