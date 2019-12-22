package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Domain extends SwitcherElement {
	
	private Group[] group;
	
	public Domain() {}

	public Group[] getGroup() {
		
		return group;
	}

	public void setGroup(Group[] group) {
		
		this.group = group;
	}

	@Override
	public String toString() {
		
		return "Domain [group=" + Arrays.toString(group) + ", description=" + description + ", activated=" + activated
				+ "]";
	}
	
}
