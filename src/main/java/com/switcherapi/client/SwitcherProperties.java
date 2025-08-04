package com.switcherapi.client;

import com.switcherapi.client.model.ContextKey;

import java.util.Properties;

/**
 * The configuration definition object contains all necessary SDK properties to
 * control the API client state.
 *
 * @author Roger Floriano (petruki)
 */
public interface SwitcherProperties {

	/**
	 * Load properties into map
	 *
	 * @param prop The properties object
	 */
	void loadFromProperties(Properties prop);

	/**
	 * Get a value (string) from the properties map
	 *
	 * @param contextKey The context key
	 * @return The value
	 */
	String getValue(ContextKey contextKey);

	/**
	 * Get a value (integer) from the properties map
	 *
	 * @param contextKey The context key
	 * @return The value
	 */
	Integer getInt(ContextKey contextKey);

	/**
	 * Get a value (boolean) from the properties map
	 *
	 * @param contextKey The context key
	 * @return The value
	 */
	boolean getBoolean(ContextKey contextKey);

	/**
	 * Set a value into the properties map
	 *
	 * @param contextKey The context key
	 * @param value The value
	 */
	void setValue(ContextKey contextKey, Object value);
}
