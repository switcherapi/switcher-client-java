package com.github.switcherapi.client;

import static com.github.switcherapi.client.model.SwitcherContextParam.APIKEY;
import static com.github.switcherapi.client.model.SwitcherContextParam.COMPONENT;
import static com.github.switcherapi.client.model.SwitcherContextParam.CONTEXT_LOCATION;
import static com.github.switcherapi.client.model.SwitcherContextParam.DOMAIN;
import static com.github.switcherapi.client.model.SwitcherContextParam.ENVIRONMENT;
import static com.github.switcherapi.client.model.SwitcherContextParam.OFFLINE_MODE;
import static com.github.switcherapi.client.model.SwitcherContextParam.RETRY_AFTER;
import static com.github.switcherapi.client.model.SwitcherContextParam.SILENT_MODE;
import static com.github.switcherapi.client.model.SwitcherContextParam.SNAPSHOT_AUTO_LOAD;
import static com.github.switcherapi.client.model.SwitcherContextParam.SNAPSHOT_FILE;
import static com.github.switcherapi.client.model.SwitcherContextParam.SNAPSHOT_LOCATION;
import static com.github.switcherapi.client.model.SwitcherContextParam.SNAPSHOT_SKIP_VALIDATION;
import static com.github.switcherapi.client.model.SwitcherContextParam.URL;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.SwitcherContextParam;
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
        setContextLocation(SwitcherUtils.resolveProperties(CONTEXT_LOCATION, prop));
        setUrl(SwitcherUtils.resolveProperties(URL, prop));
		setApiKey(SwitcherUtils.resolveProperties(APIKEY, prop));
		setDomain(SwitcherUtils.resolveProperties(DOMAIN, prop));
		setComponent(SwitcherUtils.resolveProperties(COMPONENT, prop));
		setEnvironment(SwitcherUtils.resolveProperties(ENVIRONMENT, prop));
		setSnapshotFile(SwitcherUtils.resolveProperties(SNAPSHOT_FILE, prop));
		setSnapshotLocation(SwitcherUtils.resolveProperties(SNAPSHOT_LOCATION, prop));
		setSnapshotSkipValidation(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SNAPSHOT_SKIP_VALIDATION, prop)));
		setSnapshotAutoLoad(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SNAPSHOT_AUTO_LOAD, prop)));
		setSilentMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(SILENT_MODE, prop)));
		setOfflineMode(Boolean.parseBoolean(SwitcherUtils.resolveProperties(OFFLINE_MODE, prop)));
		setRetryAfter(SwitcherUtils.resolveProperties(RETRY_AFTER, prop));
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
