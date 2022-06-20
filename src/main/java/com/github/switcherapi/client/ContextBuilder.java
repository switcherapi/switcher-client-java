package com.github.switcherapi.client;

public class ContextBuilder {
	
	private static ContextBuilder context;
	
	private SwitcherProperties properties;
	
	private ContextBuilder() {
		properties = new SwitcherProperties();
	}
	
	public static void preConfigure(SwitcherProperties switcherProperties) {
		if (context == null)
			context = new ContextBuilder();
		
		context.preBuild(switcherProperties);
	}
	
	public static ContextBuilder builder() {
		if (context == null)
			context = new ContextBuilder();
		
		return context;
	}
	
	void preBuild(SwitcherProperties properties) {
		this.properties = properties;
	}
	
	SwitcherProperties build() {
		return this.properties;
	}
	
	public ContextBuilder contextLocation(String contextLocation) {
		properties.setContextLocation(contextLocation);
		return this;
	}
	
	public ContextBuilder url(String url) {
		properties.setUrl(url);
		return this;
	}
	
	public ContextBuilder apiKey(String apiKey) {
		properties.setApiKey(apiKey);
		return this;
	}
	
	public ContextBuilder domain(String domain) {
		properties.setDomain(domain);
		return this;
	}
	
	public ContextBuilder component(String component) {
		properties.setComponent(component);
		return this;
	}
	
	public ContextBuilder environment(String environment) {
		properties.setEnvironment(environment);
		return this;
	}
	
	public ContextBuilder snapshotLocation(String snapshotLocation) {
		properties.setSnapshotLocation(snapshotLocation);
		return this;
	}

	public ContextBuilder snapshotFile(String snapshotFile) {
		properties.setSnapshotFile(snapshotFile);
		return this;
	}

	public ContextBuilder retryAfter(String retryAfter) {
		properties.setRetryAfter(retryAfter);
		return this;
	}

	public ContextBuilder snapshotAutoLoad(boolean snapshotAutoLoad) {
		properties.setSnapshotAutoLoad(snapshotAutoLoad);
		return this;
	}
	
	public ContextBuilder snapshotSkipValidation(boolean snapshotSkipValidation) {
		properties.setSnapshotSkipValidation(snapshotSkipValidation);
		return this;
	}

	public ContextBuilder silentMode(boolean silentMode) {
		properties.setSilentMode(silentMode);
		return this;
	}

	public ContextBuilder offlineMode(boolean offlineMode) {
		properties.setOfflineMode(offlineMode);
		return this;
	}

}
