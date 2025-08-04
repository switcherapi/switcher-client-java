package com.switcherapi.client.model.criteria;

/**
 * 
 * @author Roger Floriano (petruki)
 * @since 2020--5-9 - v1.0.2
 */
public class Snapshot {

	private Data data;

	public Domain getDomain() {
		return data != null ? data.getDomain() : new Domain();
	}

	public Data getData() {
		return data;
	}

	public void setData(Data data) {
		this.data = data;
	}

}
