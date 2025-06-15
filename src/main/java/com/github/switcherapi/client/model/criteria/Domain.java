package com.github.switcherapi.client.model.criteria;

import java.util.Arrays;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class Domain extends SwitcherElement {

	private final String name;

	private final long version;

	private final Group[] group;

	public Domain(String name, String description, boolean activated, long version, Group[] group) {
		super(description, activated);
		this.name = name;
		this.version = version;
		this.group = group;
	}

	public Domain() {
		this(null, null, false, 0L, null);
	}

	public Group[] getGroup() {
		return group;
	}

	public String getName() {
		return name;
	}

	public long getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("Domain [name = %s, description = %s, activated = %s, version = %s, group = %s]", name,
				description, activated, version, Arrays.toString(group));
	}

}
