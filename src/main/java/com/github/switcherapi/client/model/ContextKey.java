package com.github.switcherapi.client.model;

/**
 * Contains required and optional properties to build the context
 * 
 * @author Roger Floriano {petruki)
 * @since 2022-06-19
 */
public enum ContextKey {
	
	/**
	 *  (String) Switcher API URL.
	 */
	URL("switcher.url", "url"),
	
	/**
	 * (String) API Key generated by your domain.
	 */
	APIKEY("switcher.apikey", "apiKey"),
	
	/**
	 * (String) Registered domain name.
	 */
	DOMAIN("switcher.domain", "domain"),
	
	/**
	 * (String) Name of this application. This value is used to track which applications are using switchers.
	 */
	COMPONENT("switcher.component", "component"),
	
	/**
	 * (String) Name of the environment where this application is running ('default' is a production environment).
	 */
	ENVIRONMENT("switcher.environment", "environment"),
	
	/**
	 * (String) Folder path where all snapshot files are located.
	 */
	SNAPSHOT_LOCATION("switcher.snapshot.location", "snapshotLocation"),
	
	/**
	 * (String) Defines the package and class where a context wrapper is located.
	 * It is only necessary to use with {@link com.github.switcherapi.client.SwitcherContext}
	 */
	CONTEXT_LOCATION("switcher.context", "context"),
	
	/**
	 * (boolean) Activate snapshot autoload which will try to retrieve the snapshot from the API if the file does not exist.
	 */
	SNAPSHOT_AUTO_LOAD("switcher.snapshot.auto", "snapshotAutoLoad"),
	
	/**
	 * (boolean) When true it will skip validateSnapshot() (default is false)
	 */
	SNAPSHOT_SKIP_VALIDATION("switcher.snapshot.skipvalidation", "snapshotSkipValidation"),

	/**
	 * (String) Interval given to the library to update the snapshot
	 */
	SNAPSHOT_AUTO_UPDATE_INTERVAL("switcher.snapshot.updateinterval", "snapshotAutoUpdateInterval"),
	
	/**
	 * (String) Defines if client will work in silent mode by specifying the time interval to retry
	 */
	SILENT_MODE("switcher.silent", "silentMode"),
	
	/**
	 * (boolean) Defines if client will work locally.
	 */
	LOCAL_MODE("switcher.local", "local"),

	/**
	 * (Number) Defines the Timed Match regex time out.
	 */
	REGEX_TIMEOUT("switcher.regextimeout", "regexTimeout"),

	/**
	 * (Path) Defines the path for the trustsore file.
	 */
	TRUSTSTORE_PATH("switcher.truststore.path", "truststorePath"),

	/**
	 * (String) Defines the password for the truststore file.
	 */
	TRUSTSTORE_PASSWORD("switcher.truststore.password", "truststorePassword"),

	/**
	 * (Number) Defines the timeout in ms for the Remote client.
	 */
	TIMEOUT_MS("switcher.timeout", "timeoutMs");
	
	private final String param;
	private final String propField;
	
	ContextKey(String param, String propField) {
		this.param = param;
		this.propField = propField;
	}

	public String getParam() {
		return param;
	}

	public String getPropField() {
		return propField;
	}
	
}
