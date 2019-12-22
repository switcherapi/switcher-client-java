package com.switcher.client.domain.criteria;

import java.util.Arrays;

public class Domain extends SwitcherElement {
	
	private Group[] group;

	public Group[] getGroup() {
		
		return group;
	}

	@Override
	public String toString() {
		
		return "Domain [group=" + Arrays.toString(group) + ", description=" + description + ", activated=" + activated
				+ "]";
	}
	
}
