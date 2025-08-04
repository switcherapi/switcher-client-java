package com.switcherapi.client;

import com.switcherapi.client.model.ContextKey;

abstract class SwitcherConfig {

	protected String url;
	protected String apikey;
	protected String domain;
	protected String component;
	protected String environment;

	protected boolean local;
	protected boolean check;
	protected String silent;
	protected Integer timeout;
	protected Integer regexTimeout;
	protected Integer poolSize;
	protected RelayConfig relay;
	protected SnapshotConfig snapshot;
	protected TruststoreConfig truststore;

	SwitcherConfig() {
		this.relay = new RelayConfig();
		this.snapshot = new SnapshotConfig();
		this.truststore = new TruststoreConfig();
	}

	/**
	 * Update Switcher Configurations state using pre-configured properties.
	 *
	 * @param properties Switcher Properties
	 */
	protected void updateSwitcherConfig(SwitcherProperties properties) {
		setUrl(properties.getValue(ContextKey.URL));
		setApikey(properties.getValue(ContextKey.APIKEY));
		setDomain(properties.getValue(ContextKey.DOMAIN));
		setComponent(properties.getValue(ContextKey.COMPONENT));
		setEnvironment(properties.getValue(ContextKey.ENVIRONMENT));
		setLocal(properties.getBoolean(ContextKey.LOCAL_MODE));
		setCheck(properties.getBoolean(ContextKey.CHECK_SWITCHERS));
		setSilent(properties.getValue(ContextKey.SILENT_MODE));
		setTimeout(properties.getInt(ContextKey.TIMEOUT_MS));
		setPoolSize(properties.getInt(ContextKey.POOL_CONNECTION_SIZE));

		RelayConfig relayConfig = new RelayConfig();
		relayConfig.setRestrict(properties.getBoolean(ContextKey.RESTRICT_RELAY));
		setRelay(relayConfig);

		SnapshotConfig snapshotConfig = new SnapshotConfig();
		snapshotConfig.setLocation(properties.getValue(ContextKey.SNAPSHOT_LOCATION));
		snapshotConfig.setAuto(properties.getBoolean(ContextKey.SNAPSHOT_AUTO_LOAD));
		snapshotConfig.setSkipValidation(properties.getBoolean(ContextKey.SNAPSHOT_SKIP_VALIDATION));
		snapshotConfig.setWatcher(properties.getBoolean(ContextKey.SNAPSHOT_WATCHER));
		snapshotConfig.setUpdateInterval(properties.getValue(ContextKey.SNAPSHOT_AUTO_UPDATE_INTERVAL));
		setSnapshot(snapshotConfig);

		TruststoreConfig truststoreConfig = new TruststoreConfig();
		truststoreConfig.setPath(properties.getValue(ContextKey.TRUSTSTORE_PATH));
		truststoreConfig.setPassword(properties.getValue(ContextKey.TRUSTSTORE_PASSWORD));
		setTruststore(truststoreConfig);
	}

	/**
	 * Initialize the Switcher Client.<br>
	 * - Build context {@link ContextBuilder}<br>
	 * - Configure context {@link SwitcherContextBase#configure(ContextBuilder)}<br>
	 * - Initialize client {@link SwitcherContextBase#initializeClient()}<br>
	 */
	protected abstract void configureClient();

	/**
	 * Initialize the Switcher Client using a context properties file.<br>
	 * - Load context properties file {@link SwitcherContextBase#loadProperties(String)}<br>
	 * - Initialize client {@link SwitcherContextBase#initializeClient()}<br>
	 *
	 * @param contextFile path to the context file
	 */
	protected abstract void configureClient(String contextFile);

	public void setUrl(String url) {
		this.url = url;
	}

	public void setApikey(String apikey) {
		this.apikey = apikey;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public void setLocal(boolean local) {
		this.local = local;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public void setSilent(String silent) {
		this.silent = silent;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public void setRegexTimeout(Integer regexTimeout) {
		this.regexTimeout = regexTimeout;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	public void setRelay(RelayConfig relay) {
		this.relay = relay;
	}
	public void setSnapshot(SnapshotConfig snapshot) {
		this.snapshot = snapshot;
	}

	public void setTruststore(TruststoreConfig truststore) {
		this.truststore = truststore;
	}

	public static class RelayConfig {
		private boolean restrict;

		public boolean isRestrict() {
			return restrict;
		}

		public void setRestrict(boolean restrict) {
			this.restrict = restrict;
		}
	}

	public static class SnapshotConfig {
		private String location;
		private boolean auto;
		private boolean skipValidation;
		private boolean watcher;
		private String updateInterval;

		public String getLocation() {
			return location;
		}

		public void setLocation(String location) {
			this.location = location;
		}

		public boolean isAuto() {
			return auto;
		}

		public void setAuto(boolean auto) {
			this.auto = auto;
		}

		public boolean isSkipValidation() {
			return skipValidation;
		}

		public void setSkipValidation(boolean skipValidation) {
			this.skipValidation = skipValidation;
		}

		public boolean isWatcher() {
			return watcher;
		}

		public void setWatcher(boolean watcher) {
			this.watcher = watcher;
		}

		public String getUpdateInterval() {
			return updateInterval;
		}

		public void setUpdateInterval(String updateInterval) {
			this.updateInterval = updateInterval;
		}
	}

	public static class TruststoreConfig {
		private String path;
		private String password;

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
	}
}
