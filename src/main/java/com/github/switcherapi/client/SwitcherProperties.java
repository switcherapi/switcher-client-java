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
 * <p>
 * Properties are defined by {@link SwitcherContextParam}
 * 
 * @author Roger Floriano (petruki)
 */
class SwitcherProperties {

	public static final String DEFAULTURL = "https://switcher-api.herokuapp.com";

	public static final String DEFAULTENV = "default";

	private String contextLocation;

	private String url;

	private String apiKey;

	private String domain;

	private String component;

	private String environment;

	private String snapshotLocation;

	private String snapshotFile;

	private String retryAfter;

	private boolean snapshotAutoLoad;

	private boolean snapshotSkipValidation;

	private boolean silentMode;

	private boolean offlineMode;

	public SwitcherProperties() {
		this.url = DEFAULTURL;
		this.environment = DEFAULTENV;
	}
	
	public void loadFromProperties(Properties prop) {
        setContextLocation(SwitcherUtils.resolveProperties(ContextKey.CONTEXT_LOCATION.getParam(), prop));
        setUrl(SwitcherUtils.resolveProperties(ContextKey.URL.getParam(), prop));
		setApiKey(SwitcherUtils.resolveProperties(ContextKey.APIKEY.getParam(), prop));
		setDomain(SwitcherUtils.resolveProperties(ContextKey.DOMAIN.getParam(), prop));
		setComponent(SwitcherUtils.resolveProperties(ContextKey.COMPONENT.getParam(), prop));
		setEnvironment(SwitcherUtils.resolveProperties(ContextKey.ENVIRONMENT.getParam(), prop));
		setSnapshotFile(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_FILE.getParam(), prop));
		setSnapshotLocation(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_LOCATION.getParam(), prop));
		setSnapshotSkipValidation(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_SKIP_VALIDATION.getParam(), prop)));
		setSnapshotAutoLoad(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SNAPSHOT_AUTO_LOAD.getParam(), prop)));
		setSilentMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.SILENT_MODE.getParam(), prop)));
		setOfflineMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(ContextKey.OFFLINE_MODE.getParam(), prop)));
		setRetryAfter(SwitcherUtils.resolveProperties(ContextKey.RETRY_AFTER.getParam(), prop));
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
		if (!StringUtils.isBlank(url))
			this.url = url;
		else
			this.url = DEFAULTURL;
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
		if (!StringUtils.isBlank(environment))
			this.environment = environment;
		else
			this.environment = DEFAULTENV;
	}

	public String getSnapshotLocation() {
		return snapshotLocation;
	}

	public void setSnapshotLocation(String snapshotLocation) {
		this.snapshotLocation = snapshotLocation;
	}

	public String getSnapshotFile() {
		return snapshotFile;
	}

	public void setSnapshotFile(String snapshotFile) {
		this.snapshotFile = snapshotFile;
	}

	public String getRetryAfter() {
		return retryAfter;
	}

	public void setRetryAfter(String retryAfter) {
		this.retryAfter = retryAfter;
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

	public boolean isSilentMode() {
		return silentMode;
	}

	public void setSilentMode(boolean silentMode) {
		this.silentMode = silentMode;
	}

	public boolean isOfflineMode() {
		return offlineMode;
	}

	public void setOfflineMode(boolean offlineMode) {
		this.offlineMode = offlineMode;
	}

}
