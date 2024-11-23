package com.github.switcherapi.client;

abstract class SwitcherConfig {

	protected String contextLocation;
	protected String url;
	protected String apikey;
	protected String domain;
	protected String component;
	protected String environment;

	protected boolean local;
	protected String silent;
	protected Integer timeout;
	protected Integer poolSize;
	protected SnapshotConfig snapshot;
	protected TruststoreConfig truststore;

	SwitcherConfig() {
		this.snapshot = new SnapshotConfig();
		this.truststore = new TruststoreConfig();
	}

	/**
	 * Initialize the Switcher Client.<br>
	 * - Build context {@link ContextBuilder}<br>
	 * - Configure context {@link SwitcherContextBase#configure(ContextBuilder)}<br>
	 * - Initialize client {@link SwitcherContextBase#initializeClient()}<br>
	 */
	protected abstract void configureClient();

	public void setContextLocation(String contextLocation) {
		this.contextLocation = contextLocation;
	}

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

	public void setSilent(String silent) {
		this.silent = silent;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public void setPoolSize(Integer poolSize) {
		this.poolSize = poolSize;
	}

	public void setSnapshot(SnapshotConfig snapshot) {
		this.snapshot = snapshot;
	}

	public void setTruststore(TruststoreConfig truststore) {
		this.truststore = truststore;
	}

	public static class SnapshotConfig {
		private String location;
		private boolean auto;
		private boolean skipValidation;
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
