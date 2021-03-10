package com.github.switcherapi.client.model.criteria;

import java.util.Set;

import com.github.switcherapi.client.ws.ClientWS;

/**
 * Request/Response model to use with {@link ClientWS#checkSwitchers(Set, String)}
 * 
 * @author Roger Floriano (petruki)
 * @since 2021-03-09
 */
public class SwitchersCheck {
	
	private String[] switchers;
	private String[] not_found;
	
	public SwitchersCheck() {}
	
	public SwitchersCheck(final Set<String> switchers) {
		this.switchers = switchers.toArray(new String[switchers.size()]);
	}

	public String[] getSwitchers() {
		return switchers;
	}

	public void setSwitchers(String[] switchers) {
		this.switchers = switchers;
	}

	public String[] getNot_found() {
		return not_found;
	}

	public void setNot_found(String[] not_found) {
		this.not_found = not_found;
	}

}
