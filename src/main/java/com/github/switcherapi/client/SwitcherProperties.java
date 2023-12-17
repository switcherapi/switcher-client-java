package com.github.switcherapi.client;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.utils.SwitcherUtils;

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

	private String contextLocation;

	private String url;

	private String apiKey;

	private String domain;

	private String component;

	private String environment;

	private String snapshotLocation;

	private String snapshotAutoUpdateInterval;

	private String regexTimeout;

	private String silentMode;

	private boolean snapshotAutoLoad;

	private boolean snapshotSkipValidation;

	private boolean local;

	private String truststorePath;

	private String truststorePassword;

	private String timeoutMs;

	public SwitcherProperties() {
		this.environment = DEFAULT_ENV;
		this.regexTimeout = DEFAULT_REGEX_TIMEOUT;
		this.timeoutMs = DEFAULT_TIMEOUT_MS;
	}
	
	public void loadFromProperties(Properties prop) {
        setContextLocation(SwitcherUtils.resolveProperties(ContextKey.CONTEXT_LOCATION.getParam(), prop));
        setUrl(SwitcherUtils.resolveProperties(ContextKey.URL.getParam(), prop));
		setApiKey(SwitcherUtils.resolveProperties(ContextKey.APIKEY.getParam(), prop));
		setDomain(SwitcherUtils.resolveProperties(ContextKey.DOMAIN.getParam(), prop));
		setComponent(SwitcherUtils.resolveProperties(ContextKey.COMPONENT.getParam(), prop));
		setEnvironment(SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop));
		setSnapshotLocation(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_LOCATION.getParam(), prop));
		setSnapshotSkipValidation(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_SKIP_VALIDATION.getParam(), prop)));
		setSnapshotAutoLoad(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_LOAD.getParam(), prop)));
		setSnapshotAutoUpdateInterval(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL.getParam(), prop));
		setSilentMode(SwitcherUtils.resolveProperties(ContextKey.SILENT_MODE.getParam(), prop));
		setLocal(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.LOCAL_MODE.getParam(), prop)));
		setRegexTimeout(SwitcherUtils.resolveProperties(ContextKey.REGEX_TIMEOUT.getParam(), prop));
		setTruststorePath(SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PATH.getParam(), prop));
		setTruststorePassword(SwitcherUtils.resolveProperties(ContextKey.TRUSTSTORE_PASSWORD.getParam(), prop));
		setTimeoutMs(SwitcherUtils.resolveProperties(ContextKey.TIMEOUT_MS.getParam(), prop));
	}
	
	public <T> T getValue(ContextKey contextKey, Class<T> type) {
		try {
			final Field field = SwitcherProperties.class.getDeclaredField(contextKey.getPropField());
			return type.cast(field.get(this));
		} catch (Exception e) {
			throw new SwitcherContextException(e.getMessage());
		}
	}

	public String getContextLocation() {
		return contextLocation;
	}

	public void setContextLocation(String contextLocation) {
		this.contextLocation = contextLocation;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		if (StringUtils.isNotBlank(environment)) {
			this.environment = environment;
		} else {
			this.environment = DEFAULT_ENV;
		}
	}

	public String getSnapshotLocation() {
		return snapshotLocation;
	}

	public void setSnapshotLocation(String snapshotLocation) {
		this.snapshotLocation = snapshotLocation;
	}

	public String getSnapshotAutoUpdateInterval() {
		return snapshotAutoUpdateInterval;
	}

	public void setSnapshotAutoUpdateInterval(String snapshotAutoUpdateInterval) {
		this.snapshotAutoUpdateInterval = snapshotAutoUpdateInterval;
	}

	public String getRegexTimeout() {
		return regexTimeout;
	}

	public void setRegexTimeout(String regexTimeout) {
		if (StringUtils.isNotBlank(regexTimeout)) {
			this.regexTimeout = regexTimeout;
		} else {
			this.regexTimeout = DEFAULT_REGEX_TIMEOUT;
		}
	}

	public boolean isSnapshotAutoLoad() {
		return snapshotAutoLoad;
	}

	public void setSnapshotAutoLoad(boolean snapshotAutoLoad) {
		this.snapshotAutoLoad = snapshotAutoLoad;
	}

	public boolean isSnapshotSkipValidation() {
		return snapshotSkipValidation;
	}

	public void setSnapshotSkipValidation(boolean snapshotSkipValidation) {
		this.snapshotSkipValidation = snapshotSkipValidation;
	}

	public String getSilentMode() {
		return silentMode;
	}

	public void setSilentMode(String silentMode) {
		this.silentMode = silentMode;
	}

	public boolean isLocal() {
		return local;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public String getTruststorePath() {
		return truststorePath;
	}

	public void setTruststorePath(String truststorePath) {
		this.truststorePath = truststorePath;
	}

	public String getTruststorePassword() {
		return truststorePassword;
	}

	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}

	public String getTimeoutMs() {
		return timeoutMs;
	}

    public void setTimeoutMs(String timeoutMs) {
		if (StringUtils.isNotBlank(timeoutMs)) {
			this.timeoutMs = timeoutMs;
		} else {
			this.timeoutMs = DEFAULT_TIMEOUT_MS;
		}
	}

}
