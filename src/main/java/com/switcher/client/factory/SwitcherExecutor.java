package com.switcher.client.factory;

import java.util.Map;

import com.switcher.client.domain.Switcher;

public interface SwitcherExecutor {
	
	public boolean executeCriteria(final Switcher switcher) throws Exception;
	
	public void updateContext(final Map<String, Object> properties);

}
