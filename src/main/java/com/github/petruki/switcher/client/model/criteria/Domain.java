package com.github.petruki.switcher.client.model.criteria;

import java.util.Arrays;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class Domain extends SwitcherElement {
	
	private String name;
	
	private long version;
	
	private Group[] group;

	public Group[] getGroup() {
		
		return group;
	}

	public void setGroup(Group[] group) {
		
		this.group = group;
	}

	public String getName() {
		
		return name;
	}

	public void setName(String name) {
		
		this.name = name;
	}

	public long getVersion() {
		
		return version;
	}

	public void setVersion(long version) {
		
		this.version = version;
	}

	@Override
	public String toString() {
		
		return String.format("Domain [name = %s, description = %s, activated = %s, version = %s, group = %s]", 
				name, description, activated, version, Arrays.toString(group));
	}
	
}
