package com.switcherapi.client.remote.dto;

import java.util.Arrays;
import java.util.Set;

import com.switcherapi.client.remote.ClientWS;
import com.google.gson.annotations.SerializedName;

/**
 * Request/Response model to use with {@link ClientWS#checkSwitchers(Set, String)}
 * 
 * @author Roger Floriano (petruki)
 * @since 2021-03-09
 */
public class SwitchersCheck {
	
	/**
	 * Request field
	 */
	private String[] switchers;
	
	/**
	 * Response field
	 */
	@SerializedName("not_found")
	private String[] notFound;
	
	public SwitchersCheck() {}
	
	public SwitchersCheck(final Set<String> switchers) {
		this.switchers = switchers.toArray(new String[0]);
	}

	public String[] getSwitchers() {
		return switchers;
	}

	public void setSwitchers(String[] switchers) {
		this.switchers = switchers;
	}

	public String[] getNotFound() {
		return notFound;
	}

	public void setNotFound(String[] notFound) {
		this.notFound = notFound;
	}

	@Override
	public String toString() {
		return "SwitchersCheck [switchers=" + Arrays.toString(switchers) + 
				", notFound=" + Arrays.toString(notFound) + "]";
	}

}
