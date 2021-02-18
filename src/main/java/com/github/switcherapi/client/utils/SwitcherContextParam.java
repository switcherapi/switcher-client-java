package com.github.switcherapi.client.utils;

/**
 * Contains required and optional keys to set up the context
 * 
 * @author Roger Floriano {petruki)
 * @since 2019-12-24
 */
public interface SwitcherContextParam {
	
	/**
	 *  (String) Switcher API URL.
	 */
	String URL = "switcher.url";
	
	/**
	 * (String) API Key generated by your domain.
	 */
	String APIKEY = "switcher.apikey";
	
	/**
	 * (String) Registered domain name.
	 */
	String DOMAIN = "switcher.domain";
	
	/**
	 * (String) Name of this application. This value is used to track which applications are using switchers.
	 */
	String COMPONENT = "switcher.component";
	
	/**
	 * (String) Name of the environment where this application is running ('default' is a production environment).
	 */
	String ENVIRONMENT = "switcher.environment";
	
	/**
	 * (String) The absolute path of the snapshot file, including the file's name.
	 */
	String SNAPSHOT_FILE = "switcher.snapshot.file";
	
	/**
	 * (String) Folder path where all snapshot files are located.
	 */
	String SNAPSHOT_LOCATION = "switcher.snapshot.location";
	
	/**
	 * (String) Defines the package and class where a context wrapper is located.
	 * It is only necessary to use with {@link com.github.switcherapi.client.configuration.SwitcherContext}
	 */
	String CONTEXT_LOCATION = "switcher.context";
	
	/**
	 * (boolean) Activate snapshot autoload which will try to retrieve the snapshot from the API if the file does not exist.
	 */
	String SNAPSHOT_AUTO_LOAD = "switcher.snapshot.auto";
	
	/**
	 * (boolean) Activate silent mode when the Switcher API becomes offline.
	 */
	String SILENT_MODE = "switcher.silent";
	
	/**
	 * (String) Time given to the library retry reaching the online Switcher API after using the silent mode.
	 */
	String RETRY_AFTER = "switcher.retry";
	
	/**
	 * (boolean) Defines if client will work offline.
	 */
	String OFFLINE_MODE = "switcher.offline";
	
}
