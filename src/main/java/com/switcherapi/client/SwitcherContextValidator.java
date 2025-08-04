package com.switcherapi.client;

import com.switcherapi.client.exception.SwitcherContextException;
import com.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to validate SwitcherProperties parameters
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-17
 */
class SwitcherContextValidator {

	public static final String ERR_URL = "URL not defined [add: switcher.url]";
	public static final String ERR_API = "API Key not defined [add: switcher.apikey]";
	public static final String ERR_DOMAIN = "Domain not defined [add: switcher.domain]";
	public static final String ERR_COMPONENT = "Component not defined [add: switcher.component]";
	public static final String ERR_CONTEXT = "Context class location not defined [add: switcher.context]";
	public static final String ERR_LOCAL = "Snapshot location not defined [add: switcher.snapshot.location] " +
			"or enable auto-load [add: switcher.snapshot.auto]";
	
	private SwitcherContextValidator() {}

	/**
	 * Validate context properties before executing any Switcher operation
	 * 
	 * @param prop Configured properties
	 * @throws SwitcherContextException if validation fails
	 */
	public static void validate(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getValue(ContextKey.CONTEXT_LOCATION))) {
			throw new SwitcherContextException(ERR_CONTEXT);
		}
		
		if (!prop.getBoolean(ContextKey.LOCAL_MODE)) {
			validateRemote(prop);
		} else {
			validateLocal(prop);
		}
	}

	/**
	 * Validate context properties required to run remote
	 * 
	 * @param prop Configured properties
	 */
	public static void validateRemote(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getValue(ContextKey.URL))) {
			throw new SwitcherContextException(ERR_URL);
		}

		if (StringUtils.isBlank(prop.getValue(ContextKey.APIKEY))) {
			throw new SwitcherContextException(ERR_API);
		}
		
		if (StringUtils.isBlank(prop.getValue(ContextKey.DOMAIN))) {
			throw new SwitcherContextException(ERR_DOMAIN);
		}
		
		if (StringUtils.isBlank(prop.getValue(ContextKey.COMPONENT))) {
			throw new SwitcherContextException(ERR_COMPONENT);
		}
	}

	/**
	 * Validate context properties required to run local
	 *
	 * @param prop Configured properties
	 */
	public static void validateLocal(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getValue(ContextKey.SNAPSHOT_LOCATION)) &&
				!prop.getBoolean(ContextKey.SNAPSHOT_AUTO_LOAD)) {
			throw new SwitcherContextException(ERR_LOCAL);
		}
	}
}
