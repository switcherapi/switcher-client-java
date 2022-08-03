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
	 * (String) The absolute path of the snapshot file, including the file's name.
	 */
	SNAPSHOT_FILE("switcher.snapshot.file", "snapshotFile"),
	
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
	 * (boolean) Activate silent mode when the Switcher API becomes offline.
	 */
	SILENT_MODE("switcher.silent", "silentMode"),
	
	/**
	 * (String) Time given to the library retry reaching the online Switcher API after using the silent mode.
	 */
	RETRY_AFTER("switcher.retry", "retryAfter"),
	
	/**
	 * (boolean) Defines if client will work offline.
	 */
	OFFLINE_MODE("switcher.offline", "offlineMode");
	
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
