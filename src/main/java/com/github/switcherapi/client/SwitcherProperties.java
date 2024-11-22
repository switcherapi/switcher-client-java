package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.github.switcherapi.client.remote.Constants.DEFAULT_POOL_SIZE;

/**
 * The configuration definition object contains all necessary SDK properties to
 * control the API client behaviors, access and snapshot location.
 * 
 * @author Roger Floriano (petruki)
 */
public class SwitcherProperties {

	public static final String DEFAULT_ENV = "default";

	public static final Integer DEFAULT_REGEX_TIMEOUT = 3000;

	public static final Integer DEFAULT_TIMEOUT_MS = 3000;

	private final Map<String, Object> properties = new HashMap<>();

	public SwitcherProperties() {
		setDefaults();
	}

	private void setDefaults() {
		setValue(ContextKey.ENVIRONMENT, DEFAULT_ENV);
		setValue(ContextKey.REGEX_TIMEOUT, DEFAULT_REGEX_TIMEOUT);
		setValue(ContextKey.TIMEOUT_MS, DEFAULT_TIMEOUT_MS);
		setValue(ContextKey.POOL_CONNECTION_SIZE, DEFAULT_POOL_SIZE);
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
		setValue(ContextKey.ENVIRONMENT, getValueDefault(SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop), DEFAULT_ENV));
		setValue(ContextKey.SNAPSHOT_LOCATION, SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_LOCATION.getParam(), prop));
		setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, getBoolDefault(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_SKIP_VALIDATION.getParam(), prop), false));
		setValue(ContextKey.SNAPSHOT_AUTO_LOAD, getBoolDefault(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_LOAD.getParam(), prop), false));
		setValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL, SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL.getParam(), prop));
		setValue(ContextKey.SILENT_MODE, SwitcherUtils.resolveProperties(ContextKey.SILENT_MODE.getParam(), prop));
		setValue(ContextKey.LOCAL_MODE, getBoolDefault(SwitcherUtils.resolveProperties(ContextKey.LOCAL_MODE.getParam(), prop), false));
		setValue(ContextKey.REGEX_TIMEOUT, getIntDefault(SwitcherUtils.resolveProperties(ContextKey.REGEX_TIMEOUT.getParam(), prop), DEFAULT_REGEX_TIMEOUT));
		setValue(ContextKey.TRUSTSTORE_PATH, SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PATH.getParam(), prop));
		setValue(ContextKey.TRUSTSTORE_PASSWORD, SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PASSWORD.getParam(), prop));
		setValue(ContextKey.TIMEOUT_MS, getIntDefault(SwitcherUtils.resolveProperties(ContextKey.TIMEOUT_MS.getParam(), prop), DEFAULT_TIMEOUT_MS));
		setValue(ContextKey.POOL_CONNECTION_SIZE, getIntDefault(SwitcherUtils.resolveProperties(ContextKey.POOL_CONNECTION_SIZE.getParam(), prop), DEFAULT_POOL_SIZE));
	}

	public String getValue(ContextKey contextKey) {
		return getValue(contextKey, String.class);
	}

	public boolean getBoolean(ContextKey contextKey) {
		return getValue(contextKey, Boolean.class);
	}

	public Integer getInt(ContextKey contextKey) {
		return getValue(contextKey, Integer.class);
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

	public String getValueDefault(String value, String defaultValue) {
		return StringUtils.defaultIfBlank(value, defaultValue);
	}

	public Integer getIntDefault(String value, Integer defaultValue) {
		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		}

		return defaultValue;
	}

	public Boolean getBoolDefault(String value, Boolean defaultValue) {
		if (StringUtils.isNotBlank(value)) {
			return Boolean.parseBoolean(value);
		}

		return defaultValue;
	}

}
