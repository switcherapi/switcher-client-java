package com.github.switcherapi.client;

import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

import static com.github.switcherapi.client.remote.Constants.*;

public class ContextBuilder {
	
	private static ContextBuilder context;
	
	private SwitcherProperties switcherProperties;
	
	private ContextBuilder(SwitcherProperties switcherProperties) {
		this.switcherProperties = switcherProperties;
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
			context = new ContextBuilder(new SwitcherPropertiesImpl());

		return context;
	}
	
	void preBuild(SwitcherProperties properties) {
		this.switcherProperties = properties;
	}

	SwitcherProperties build() {
		return this.switcherProperties;
	}

	/**
	 * @param context Feature class that extends SwitcherContext
	 * @return ContextBuilder
	 */
	public ContextBuilder context(String context) {
		switcherProperties.setValue(ContextKey.CONTEXT_LOCATION, context);
		return this;
	}

	/**
	 * @param url Switcher API URL
	 * @return ContextBuilder
	 */
	public ContextBuilder url(String url) {
		switcherProperties.setValue(ContextKey.URL, url);
		return this;
	}

	/**
	 * @param apiKey Switcher API Key
	 * @return ContextBuilder
	 */
	public ContextBuilder apiKey(String apiKey) {
		switcherProperties.setValue(ContextKey.APIKEY, apiKey);
		return this;
	}

	/**
	 * @param domain Switcher Domain
	 * @return ContextBuilder
	 */
	public ContextBuilder domain(String domain) {
		switcherProperties.setValue(ContextKey.DOMAIN, domain);
		return this;
	}

	/**
	 * @param component Switcher Component
	 * @return ContextBuilder
	 */
	public ContextBuilder component(String component) {
		switcherProperties.setValue(ContextKey.COMPONENT, component);
		return this;
	}

	/**
	 * @param environment Switcher Environment
	 * @return ContextBuilder
	 */
	public ContextBuilder environment(String environment) {
		switcherProperties.setValue(ContextKey.ENVIRONMENT,
				Optional.ofNullable(environment).orElse(DEFAULT_ENV));
		return this;
	}

	/**
	 * @param snapshotLocation Folder path from where snapshots will be saved/read
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotLocation(String snapshotLocation) {
		switcherProperties.setValue(ContextKey.SNAPSHOT_LOCATION, snapshotLocation);
		return this;
	}

	/**
	 * @param snapshotAutoUpdateInterval Enable the Snapshot Auto Update given an interval of time - e.g. 1s (s: seconds, m: minutes)
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotAutoUpdateInterval(String snapshotAutoUpdateInterval) {
		switcherProperties.setValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL, snapshotAutoUpdateInterval);

		if (snapshotAutoUpdateInterval != null)
			switcherProperties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, true);

		return this;
	}

	/**
	 * @param snapshotAutoLoad true/false Automated lookup for snapshot when initializing the client
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotAutoLoad(boolean snapshotAutoLoad) {
		switcherProperties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, snapshotAutoLoad);
		return this;
	}

	/**
	 * @param snapshotSkipValidation true/false Skip snapshotValidation() that can be used for UT executions
	 * @return ContextBuilder
	 */
	public ContextBuilder snapshotSkipValidation(boolean snapshotSkipValidation) {
		switcherProperties.setValue(ContextKey.SNAPSHOT_SKIP_VALIDATION, snapshotSkipValidation);
		return this;
	}

	/**
	 * @param retryAfter Enable contingency given the time for the client to retry - e.g. 5s (s: seconds - m: minutes - h: hours)
	 * @return ContextBuilder
	 */
	public ContextBuilder silentMode(String retryAfter) {
		switcherProperties.setValue(ContextKey.SILENT_MODE, retryAfter);

		if (StringUtils.isNotBlank(retryAfter)) {
			switcherProperties.setValue(ContextKey.SNAPSHOT_AUTO_LOAD, true);
		}

		return this;
	}

	/**
	 * @param local true/false When local, it will only use a local snapshot
	 * @return ContextBuilder
	 */
	public ContextBuilder local(boolean local) {
		switcherProperties.setValue(ContextKey.LOCAL_MODE, local);
		return this;
	}

	/**
	 * @param truststorePath Path to the truststore file
	 * @return ContextBuilder
	 */
	public ContextBuilder truststorePath(String truststorePath) {
		switcherProperties.setValue(ContextKey.TRUSTSTORE_PATH, truststorePath);
		return this;
	}

	/**
	 * @param truststorePassword Password to the truststore file
	 * @return ContextBuilder
	 */
	public ContextBuilder truststorePassword(String truststorePassword) {
		switcherProperties.setValue(ContextKey.TRUSTSTORE_PASSWORD, truststorePassword);
		return this;
	}

	/**
	 * @param timeoutMs Time in ms given to the API to respond - 3000 default value
	 * @return ContextBuilder
	 */
    public ContextBuilder timeoutMs(Integer timeoutMs) {
		switcherProperties.setValue(ContextKey.TIMEOUT_MS,
				Optional.ofNullable(timeoutMs).orElse(DEFAULT_TIMEOUT));
		return this;
	}

	/**
	 * @param poolSize Number of threads for the pool connection - 10 default value
	 * @return ContextBuilder
	 */
	public ContextBuilder poolConnectionSize(Integer poolSize) {
		switcherProperties.setValue(ContextKey.POOL_CONNECTION_SIZE,
				Optional.ofNullable(poolSize).orElse(DEFAULT_POOL_SIZE));
		return this;
	}
}
