package com.github.petruki.switcher.client.factory;

import java.util.Map;

import com.github.petruki.switcher.client.domain.Switcher;
import com.github.petruki.switcher.client.exception.SwitcherException;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public interface SwitcherExecutor {
	
	public boolean executeCriteria(final Switcher switcher) throws SwitcherException;
	
	public void updateContext(final Map<String, Object> properties);

}
