package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

/**
 * Helper class to validate SwitcherProperties parameters
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-17
 */
class SwitcherContextValidator {

	public static final String ERR_FORMAT = "Invalid parameter format for [%s]. Expected %s.";
	public static final String ERR_URL = "URL not defined [add: switcher.url]";
	public static final String ERR_API = "API Key not defined [add: switcher.apikey]";
	public static final String ERR_DOMAIN = "Domain not defined [add: switcher.domain]";
	public static final String ERR_COMPONENT = "Component not defined [add: switcher.component]";
	public static final String ERR_CONTEXT = "Context class location not defined [add: switcher.context]";
	
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
		}
		
		validateOptionals(prop);
	}

	/**
	 * Validate optional context arguments
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOptionals(final SwitcherProperties prop) {
		try {
			Integer.parseInt(prop.getValue(ContextKey.REGEX_TIMEOUT));
		} catch (NumberFormatException e) {
			throw new SwitcherContextException(
					String.format(ERR_FORMAT, ContextKey.REGEX_TIMEOUT.getParam(), Integer.class));
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
}
