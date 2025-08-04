package com.switcherapi.client;

import com.switcherapi.client.exception.SwitcherContextException;
import com.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.switcherapi.client.remote.Constants.*;
import static com.switcherapi.client.utils.SwitcherUtils.*;

public class SwitcherPropertiesImpl implements SwitcherProperties {

	private final Map<String, Object> properties = new HashMap<>();

	public SwitcherPropertiesImpl() {
		initDefaults();
	}

	private void initDefaults() {
		setValue(ContextKey.ENVIRONMENT, DEFAULT_ENV);
		setValue(ContextKey.REGEX_TIMEOUT, DEFAULT_REGEX_TIMEOUT);
		setValue(ContextKey.TIMEOUT_MS, DEFAULT_TIMEOUT);
		setValue(ContextKey.POOL_CONNECTION_SIZE, DEFAULT_POOL_SIZE);
		setValue(ContextKey.SNAPSHOT_AUTO_LOAD, false);
		setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, false);
		setValue(ContextKey.SNAPSHOT_WATCHER, false);
		setValue(ContextKey.LOCAL_MODE, false);
		setValue(ContextKey.CHECK_SWITCHERS, false);
		setValue(ContextKey.RESTRICT_RELAY, true);
	}

	@Override
	public void loadFromProperties(Properties prop) {
		setValue(ContextKey.CONTEXT_LOCATION, resolveProperties(ContextKey.CONTEXT_LOCATION.getParam(), prop));
		setValue(ContextKey.URL, resolveProperties(ContextKey.URL.getParam(), prop));
		setValue(ContextKey.APIKEY, resolveProperties(ContextKey.APIKEY.getParam(), prop));
		setValue(ContextKey.DOMAIN, resolveProperties(ContextKey.DOMAIN.getParam(), prop));
		setValue(ContextKey.COMPONENT, resolveProperties(ContextKey.COMPONENT.getParam(), prop));
		setValue(ContextKey.ENVIRONMENT, getValueDefault(resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop), DEFAULT_ENV));
		setValue(ContextKey.SNAPSHOT_LOCATION, resolveProperties(ContextKey.SNAPSHOT_LOCATION.getParam(), prop));
		setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, getBoolDefault(resolveProperties(ContextKey.SNAPSHOT_SKIP_VALIDATION.getParam(), prop), false));
		setValue(ContextKey.SNAPSHOT_AUTO_LOAD, getBoolDefault(resolveProperties(ContextKey.SNAPSHOT_AUTO_LOAD.getParam(), prop), false));
		setValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL, resolveProperties(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL.getParam(), prop));
		setValue(ContextKey.SNAPSHOT_WATCHER, getBoolDefault(resolveProperties(ContextKey.SNAPSHOT_WATCHER.getParam(), prop), false));
		setValue(ContextKey.SILENT_MODE, resolveProperties(ContextKey.SILENT_MODE.getParam(), prop));
		setValue(ContextKey.LOCAL_MODE, getBoolDefault(resolveProperties(ContextKey.LOCAL_MODE.getParam(), prop), false));
		setValue(ContextKey.CHECK_SWITCHERS, getBoolDefault(resolveProperties(ContextKey.CHECK_SWITCHERS.getParam(), prop), false));
		setValue(ContextKey.RESTRICT_RELAY, getBoolDefault(resolveProperties(ContextKey.RESTRICT_RELAY.getParam(), prop), true));
		setValue(ContextKey.REGEX_TIMEOUT, getIntDefault(resolveProperties(ContextKey.REGEX_TIMEOUT.getParam(), prop), DEFAULT_REGEX_TIMEOUT));
		setValue(ContextKey.TRUSTSTORE_PATH, resolveProperties(ContextKey.TRUSTSTORE_PATH.getParam(), prop));
		setValue(ContextKey.TRUSTSTORE_PASSWORD, resolveProperties(ContextKey.TRUSTSTORE_PASSWORD.getParam(), prop));
		setValue(ContextKey.TIMEOUT_MS, getIntDefault(resolveProperties(ContextKey.TIMEOUT_MS.getParam(), prop), DEFAULT_TIMEOUT));
		setValue(ContextKey.POOL_CONNECTION_SIZE, getIntDefault(resolveProperties(ContextKey.POOL_CONNECTION_SIZE.getParam(), prop), DEFAULT_POOL_SIZE));
	}

	@Override
	public String getValue(ContextKey contextKey) {
		return getValue(contextKey, String.class);
	}

	@Override
	public boolean getBoolean(ContextKey contextKey) {
		return getValue(contextKey, Boolean.class);
	}

	@Override
	public Integer getInt(ContextKey contextKey) {
		return getValue(contextKey, Integer.class);
	}

	@Override
	public void setValue(ContextKey contextKey, Object value) {
		properties.put(contextKey.getParam(), value);
	}

	private <T> T getValue(ContextKey contextKey, Class<T> type) {
		try {
			return type.cast(properties.get(contextKey.getParam()));
		} catch (ClassCastException e) {
			throw new SwitcherContextException(e.getMessage());
		}
	}

	private String getValueDefault(String value, String defaultValue) {
		return StringUtils.defaultIfBlank(value, defaultValue);
	}

	private Integer getIntDefault(String value, Integer defaultValue) {
		if (StringUtils.isNotBlank(value)) {
			return Integer.parseInt(value);
		}

		return defaultValue;
	}

	private Boolean getBoolDefault(String value, Boolean defaultValue) {
		if (StringUtils.isNotBlank(value)) {
			return Boolean.parseBoolean(value);
		}

		return defaultValue;
	}

}
