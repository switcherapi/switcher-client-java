package com.github.switcherapi.client;

import java.io.File;

import com.github.switcherapi.client.model.ContextKey;
import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherContextException;

/**
 * Helper class to validate SwitcherProperties parameters
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-17
 */
class SwitcherContextValidator {

	private static final String SNAPSHOT_PATH_PATTERN = "%s/%s.json";
	public static final String ERR_FORMAT = "Invalid parameter format for [%s]. Expected %s.";
	public static final String ERR_LOCATION_SNAPSHOT_FILE = "Snapshot locations not defined [add: switcher.snapshot.location or switcher.snapshot.file]";
	public static final String ERR_SNAPSHOT_FILE = "Snapshot file not defined [add: switcher.snapshot.file]";
	public static final String ERR_SNAPSHOT_LOCATION = "Snapshot location not defined [add: switcher.snapshot.location]";
	public static final String ERR_SNAPSHOT_AUTO_UPDATE = "Snapshot Update Interval requires [switcher.url, switcher.snapshot.file/location]";
	public static final String ERR_RETRY = "Retry not defined [add: switcher.retry]";
	public static final String ERR_URL = "URL not defined [add: switcher.url]";
	public static final String ERR_API = "API Key not defined [add: switcher.apikey]";
	public static final String ERR_DOMAIN = "Domain not defined [add: switcher.domain]";
	public static final String ERR_COMPONENT = "Component not defined [add: switcher.component]";
	public static final String ERR_CONTEXT = "Context class location not defined [add: switcher.context]";
	
	private SwitcherContextValidator() {}

	/**
	 * Validate context properties before executing any Switcher operation
	 * 
	 * @param prop Configured properties
	 * @throws SwitcherContextException if validation fails
	 */
	public static void validate(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getContextLocation())) {
			throw new SwitcherContextException(ERR_CONTEXT);
		}
		
		if (!prop.isOfflineMode()) {
			validateOnline(prop);
		} else {
			validateOffline(prop);
		}
		
		validateOptionals(prop);
	}

	/**
	 * Validate Offline settings
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOffline(final SwitcherProperties prop) {
		final StringBuilder error = new StringBuilder();

		// No Snapshot File may require a Snapshot Location
		if (StringUtils.isBlank(prop.getSnapshotFile())) {
			error.append(ERR_LOCATION_SNAPSHOT_FILE);
		} else {
			final File file = new File(prop.getSnapshotFile());
			if (!file.exists()) {
				throw new SwitcherContextException(ERR_SNAPSHOT_FILE);
			} else {
				return;
			}
		}

		// No Snapshot Location may require Snapshot File
		if (StringUtils.isBlank(prop.getSnapshotLocation())) {
			if (!StringUtils.isBlank(error.toString())) {
				throw new SwitcherContextException(error.toString());
			}

		// Snapshot Autoload requires a valid Snapshot Location
		} else if (!prop.isSnapshotAutoLoad()) {
			final File folderPath = new File(prop.getSnapshotLocation());
			if (!folderPath.exists()) {
				throw new SwitcherContextException(ERR_SNAPSHOT_LOCATION);
			} else {
				final File snapshotFile = new File(
						String.format(SNAPSHOT_PATH_PATTERN, prop.getSnapshotLocation(), prop.getEnvironment()));
				
				if (!snapshotFile.exists()) {
					throw new SwitcherContextException(ERR_SNAPSHOT_LOCATION);
				}
			}
		}
	}

	/**
	 * Validate optional context arguments
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOptionals(final SwitcherProperties prop) {
		if (prop.isSnapshotAutoLoad() && StringUtils.isBlank(prop.getSnapshotLocation())) {
			throw new SwitcherContextException(ERR_SNAPSHOT_LOCATION);
		}

		if (prop.isSilentMode() && StringUtils.isBlank(prop.getRetryAfter())) {
			throw new SwitcherContextException(ERR_RETRY);
		}

		if (!StringUtils.isBlank(prop.getSnapshotAutoUpdateInterval()) &&
			(StringUtils.isBlank(StringUtils.defaultIfEmpty(prop.getSnapshotLocation(), StringUtils.EMPTY) +
				StringUtils.defaultIfEmpty(prop.getSnapshotFile(), StringUtils.EMPTY)))) {
			throw new SwitcherContextException(ERR_SNAPSHOT_AUTO_UPDATE);
		}

		try {
			Integer.parseInt(prop.getRegexTimeout());
		} catch (NumberFormatException e) {
			throw new SwitcherContextException(
					String.format(ERR_FORMAT, ContextKey.REGEX_TIMEOUT.getParam(), Integer.class));
		}
	}

	/**
	 * Validate context properties required to run online
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOnline(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getUrl())) {
			throw new SwitcherContextException(ERR_URL);
		}

		if (StringUtils.isBlank(prop.getApiKey())) {
			throw new SwitcherContextException(ERR_API);
		}
		
		if (StringUtils.isBlank(prop.getDomain())) {
			throw new SwitcherContextException(ERR_DOMAIN);
		}
		
		if (StringUtils.isBlank(prop.getComponent())) {
			throw new SwitcherContextException(ERR_COMPONENT);
		}
	}
}
