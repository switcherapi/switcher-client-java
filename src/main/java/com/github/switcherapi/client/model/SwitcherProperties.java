package com.github.switcherapi.client.model;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.utils.SwitcherContextParam;

/**
 * The configuration definition object contains all necessary SDK properties
 * to control the API client behaviors, access and snapshot location.
 * 
 * <p>
 * Properties are defined by {@link SwitcherContextParam}
 * 
 * @author Roger Floriano (petruki)
 */
public class SwitcherProperties {
	
	private final String DEFAULT_URL = "https://switcher-api.herokuapp.com";
	
	private final String DEFAULT_ENV = "default";
	
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
	
	private boolean silentMode;
	
	private boolean offlineMode;
	
	public SwitcherProperties() {
		this.url = DEFAULT_URL;
		this.environment = DEFAULT_ENV;
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
			this.url = DEFAULT_URL;
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
			this.environment = DEFAULT_ENV;
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
