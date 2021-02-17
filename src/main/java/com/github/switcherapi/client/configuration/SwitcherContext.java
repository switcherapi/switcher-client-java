package com.github.switcherapi.client.configuration;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherFactoryContextException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.utils.SwitcherContextParam;

/**
 * <b>Switcher Context Loader</b>
 * <p>
 * 
 * This implementation is useful to organize all feature keys into one single place.
 * It also helps to configure the context.
 * 
 * @author Roger Floriano (petruki)
 */
public abstract class SwitcherContext {
	
	private static final Map<String, Object> properties;
	private static final Set<String> switchers;
	
	static {
		properties = new HashMap<>();
		switchers = new HashSet<>();
		
		properties.put(SwitcherContextParam.CONTEXT_LOCATION, "com.github.switcherapi.MyAppFeatures");
		properties.put(SwitcherContextParam.URL, "https://switcher-load-balance.herokuapp.com");
		properties.put(SwitcherContextParam.APIKEY, "$2b$08$Hm77RoqpXb.1f7izs06uKendX.B1jjWqTZsfJAzYnFoRzJpEFQXEi");
		properties.put(SwitcherContextParam.DOMAIN, "Playground");
		properties.put(SwitcherContextParam.COMPONENT, "switcher-playground");
		properties.put(SwitcherContextParam.ENVIRONMENT, "default");
		SwitcherFactory.buildContext(properties, false);
		
		loadSwitchers();
	}
	
	/**
	 * Prevalidate Switchers.
	 * It will ensure that only properly annotated Switchers can be used.
	 */
	private static void loadSwitchers() {
		try {
			final Class<?> clazz = Class.forName(properties.get(SwitcherContextParam.CONTEXT_LOCATION).toString());
			for (Field field : clazz.getFields()) {
				if (field.isAnnotationPresent(SwitcherKey.class)) {
					switchers.add(field.getName());
				}
			}
		} catch (ClassNotFoundException e) {
			throw new SwitcherFactoryContextException(e.getMessage());
		}
	}
	
	/**
	 * Return a ready-to-use Switcher that will invoke the criteria configured into the Switcher API or Snapshot
	 * 
	 * @param key name of the key created
	 * @return a ready to use Switcher
	 * @throws SwitcherKeyNotFoundException in case the key was not properly loaded
	 *  
	 * @see {@link com.github.switcherapi.client.configuration.SwitcherKey}
	 */
	public static Switcher getSwitcher(String key) {
		if (switchers.contains(key))
			return SwitcherFactory.getSwitcher(key);
		throw new SwitcherKeyNotFoundException(key);
	}
	
}
