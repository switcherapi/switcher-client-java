package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * The configuration definition object contains all necessary SDK properties to
 * control the API client behaviors, access and snapshot location.
 * 
 * @author Roger Floriano (petruki)
 */
public class SwitcherProperties {

	public static final String DEFAULT_ENV = "default";

	public static final String DEFAULT_REGEX_TIMEOUT = "3000";

	public static final String DEFAULT_TIMEOUT_MS = "3000";

	private final Map<String, Object> properties = new HashMap<>();

	public SwitcherProperties() {
		setDefaults();
	}

	private void setDefaults() {
		setValue(ContextKey.ENVIRONMENT, DEFAULT_ENV);
		setValue(ContextKey.REGEX_TIMEOUT, DEFAULT_REGEX_TIMEOUT);
		setValue(ContextKey.TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
		setValue(ContextKey.SNAPSHOT_AUTO_LOAD, false);
		setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, false);
		setValue(ContextKey.LOCAL_MODE, false);
	}
	
	public void loadFromProperties(Properties prop) {
		setValue(ContextKey.CONTEXT_LOCATION, SwitcherUtils.resolveProperties(ContextKey.CONTEXT_LOCATION.getParam(), prop));
		setValue(ContextKey.URL, SwitcherUtils.resolveProperties(ContextKey.URL.getParam(), prop));
		setValue(ContextKey.APIKEY, SwitcherUtils.resolveProperties(ContextKey.APIKEY.getParam(), prop));
		setValue(ContextKey.DOMAIN, SwitcherUtils.resolveProperties(ContextKey.DOMAIN.getParam(), prop));
		setValue(ContextKey.COMPONENT, SwitcherUtils.resolveProperties(ContextKey.COMPONENT.getParam(), prop));
		setValue(ContextKey.ENVIRONMENT, getEnvironmentOrDefault(SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop)));
		setValue(ContextKey.SNAPSHOT_LOCATION, SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_LOCATION.getParam(), prop));
		setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, getBoolDefault(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_SKIP_VALIDATION.getParam(), prop)), false));
		setValue(ContextKey.SNAPSHOT_AUTO_LOAD, getBoolDefault(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_LOAD.getParam(), prop)), false));
		setValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL, SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL.getParam(), prop));
		setValue(ContextKey.SILENT_MODE, SwitcherUtils.resolveProperties(ContextKey.SILENT_MODE.getParam(), prop));
		setValue(ContextKey.LOCAL_MODE, getBoolDefault(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.LOCAL_MODE.getParam(), prop)), false));
		setValue(ContextKey.REGEX_TIMEOUT, getRegexTimeoutOrDefault(SwitcherUtils.resolveProperties(ContextKey.REGEX_TIMEOUT.getParam(), prop)));
		setValue(ContextKey.TRUSTSTORE_PATH, SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PATH.getParam(), prop));
		setValue(ContextKey.TRUSTSTORE_PASSWORD, SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PASSWORD.getParam(), prop));
		setValue(ContextKey.TIMEOUT_MS, getTimeoutMsOrDefault(SwitcherUtils.resolveProperties(ContextKey.TIMEOUT_MS.getParam(), prop)));
	}

	public String getValue(ContextKey contextKey) {
		return getValue(contextKey, String.class);
	}

	public boolean getBoolean(ContextKey contextKey) {
		return getValue(contextKey, Boolean.class);
	}

	private <T> T getValue(ContextKey contextKey, Class<T> type) {
		try {
			return type.cast(properties.get(contextKey.getParam()));
		} catch (ClassCastException e) {
			throw new SwitcherContextException(e.getMessage());
		}
	}

	public void setValue(ContextKey contextKey, Object value) {
		properties.put(contextKey.getParam(), value);
	}

	public String getEnvironmentOrDefault(String environment) {
		if (StringUtils.isNotBlank(environment)) {
			return environment;
		}

		return DEFAULT_ENV;
	}

	public String getRegexTimeoutOrDefault(String regexTimeout) {
		if (StringUtils.isNotBlank(regexTimeout)) {
			return regexTimeout;
		}

		return DEFAULT_REGEX_TIMEOUT;
	}

	public String getTimeoutMsOrDefault(String timeoutMs) {
		if (StringUtils.isNotBlank(timeoutMs)) {
			return timeoutMs;
		}

		return DEFAULT_TIMEOUT_MS;
	}

	public Boolean getBoolDefault(Boolean value, Boolean defaultValue) {
		if (value != null) {
			return value;
		}

		return defaultValue;
	}

}
