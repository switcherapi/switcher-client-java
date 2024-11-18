package com.github.switcherapi.client;

import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

public class ContextBuilder {
	
	private static ContextBuilder context;
	
	private SwitcherProperties properties;
	
	private ContextBuilder() {
		properties = new SwitcherProperties();
	}
	
	public static void preConfigure(SwitcherProperties switcherProperties) {
		context = builder();
		context.preBuild(switcherProperties);
	}

	/**
	 * Initialize the ContextBuilder preserving the existing context
	 * @return ContextBuilder
	 */
	public static ContextBuilder builder() {
		context = builder(false);
		return context;
	}

	/**
	 * Initialize the ContextBuilder using existing context or create a new one
	 *
	 * @param init true/false to create a new context
	 * @return ContextBuilder
	 */
	public static ContextBuilder builder(boolean init) {
		if (context == null || init)
			context = new ContextBuilder();

		return context;
	}
	
	void preBuild(SwitcherProperties properties) {
		this.properties = properties;
	}
	
	SwitcherProperties build() {
		return this.properties;
	}

	/**
	 * @param contextLocation Feature class that extends SwitcherContext
	 * @return ContextBuilder
	 */
	public ContextBuilder contextLocation(String contextLocation) {
		properties.setValue(ContextKey.CONTEXT_LOCATION, contextLocation);
		return this;
	}

	/**
	 * @param url Switcher API URL
	 * @return ContextBuilder
	 */
	public ContextBuilder url(String url) {
		properties.setValue(ContextKey.URL, url);
		return this;
	}

	/**
	 * @param apiKey Switcher API Key
	 * @return ContextBuilder
	 */
	public ContextBuilder apiKey(String apiKey) {
		properties.setValue(ContextKey.APIKEY, apiKey);
		return this;
	}

	/**
	 * @param domain Switcher Domain
	 * @return ContextBuilder
	 */
	public ContextBuilder domain(String domain) {
		properties.setValue(ContextKey.DOMAIN, domain);
		return this;
	}

	/**
	 * @param component Switcher Component
	 * @return ContextBuilder
	 */
	public ContextBuilder component(String component) {
		properties.setValue(ContextKey.COMPONENT, component);
		return this;
	}

	/**
	 * @param environment Switcher Environment
	 * @return ContextBuilder
	 */
	public ContextBuilder environment(String environment) {
		properties.setValue(ContextKey.ENVIRONMENT, properties.getEnvironmentOrDefault(environment));
		return this;
	}

	/**
	 * @param snapshotLocation Folder path from where snapshots will be saved/read
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotLocation(String snapshotLocation) {
		properties.setValue(ContextKey.SNAPSHOT_LOCATION, snapshotLocation);
		return this;
	}

	/**
	 * @param snapshotAutoUpdateInterval Enable the Snapshot Auto Update given an interval of time - e.g. 1s (s: seconds, m: minutes)
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotAutoUpdateInterval(String snapshotAutoUpdateInterval) {
		properties.setValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL, snapshotAutoUpdateInterval);

		if (snapshotAutoUpdateInterval != null)
			properties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, true);

		return this;
	}

	/**
	 * Java 8 only
	 *
	 * @param regexTimeout Time in ms given to Timed Match Worker used for local Regex (ReDoS safety mechanism) - 3000 default value
	 * @return ContextBuilder
	 */
	public ContextBuilder regexTimeout(String regexTimeout) {
		properties.setValue(ContextKey.REGEX_TIMEOUT, properties.getRegexTimeoutOrDefault(regexTimeout));
		return this;
	}

	/**
	 * @param snapshotAutoLoad true/false Automated lookup for snapshot when initializing the client
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotAutoLoad(boolean snapshotAutoLoad) {
		properties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, snapshotAutoLoad);
		return this;
	}

	/**
	 * @param snapshotSkipValidation true/false Skip snapshotValidation() that can be used for UT executions
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotSkipValidation(boolean snapshotSkipValidation) {
		properties.setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, snapshotSkipValidation);
		return this;
	}

	/**
	 * @param retryAfter Enable contigency given the time for the client to retry - e.g. 5s (s: seconds - m: minutes - h: hours)
	 * @return ContextBuilder
	 */
	public ContextBuilder silentMode(String retryAfter) {
		properties.setValue(ContextKey.SILENT_MODE, retryAfter);

		if (StringUtils.isNotBlank(retryAfter)) {
			properties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, true);
		}

		return this;
	}

	/**
	 * @param local true/false When local, it will only use a local snapshot
	 * @return ContextBuilder
	 */
	public ContextBuilder local(boolean local) {
		properties.setValue(ContextKey.LOCAL_MODE, local);
		return this;
	}

	/**
	 * @param truststorePath Path to the truststore file
	 * @return ContextBuilder
	 */
	public ContextBuilder truststorePath(String truststorePath) {
		properties.setValue(ContextKey.TRUSTSTORE_PATH, truststorePath);
		return this;
	}

	/**
	 * @param truststorePassword Password to the truststore file
	 * @return ContextBuilder
	 */
	public ContextBuilder truststorePassword(String truststorePassword) {
		properties.setValue(ContextKey.TRUSTSTORE_PASSWORD, truststorePassword);
		return this;
	}

	/**
	 * @param timeoutMs Time in ms given to the API to respond - 3000 default value
	 * @return ContextBuilder
	 */
    public ContextBuilder timeoutMs(String timeoutMs) {
		properties.setValue(ContextKey.TIMEOUT_MS, properties.getTimeoutMsOrDefault(timeoutMs));
		return this;
	}
}
