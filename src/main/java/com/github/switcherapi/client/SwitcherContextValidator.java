package com.github.switcherapi.client;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherContextException;

/**
 * Helper class to validate SwitcherProperties parameters
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-17
 */
class SwitcherContextValidator {
	
	private SwitcherContextValidator() {}

	/**
	 * Validate context properties before executing any Switcher operation
	 * 
	 * @param prop Configured properties
	 * @throws SwitcherContextException if validation fails
	 */
	public static void validate(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getContextLocation())) {
			throw new SwitcherContextException("Context class location not defined [add: switcher.context]");
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
		if (StringUtils.isBlank(prop.getSnapshotFile())) {
			error.append("Snapshot locations not defined [add: switcher.snapshot.location or switcher.snapshot.file]");
		} else {
			final File file = new File(prop.getSnapshotFile());
			if (!file.exists()) {
				throw new SwitcherContextException("Snapshot file not defined [add: switcher.snapshot.file]");
			} else {
				return;
			}
		}
		
		if (StringUtils.isBlank(prop.getSnapshotLocation())) {
			if (!StringUtils.isBlank(error.toString())) {
				throw new SwitcherContextException(error.toString());
			}
		} else if (!prop.isSnapshotAutoLoad()) {
			final File file = new File(prop.getSnapshotLocation());
			if (!file.exists()) {
				throw new SwitcherContextException("Snapshot location not defined [add: switcher.snapshot.location]");
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
			throw new SwitcherContextException("Snapshot location not defined [add: switcher.snapshot.location]");
		}
		
		if (prop.isSilentMode() && StringUtils.isBlank(prop.getRetryAfter())) {
			throw new SwitcherContextException("Retry not defined [add: switcher.retry]");
		}
	}

	/**
	 * Validate context properties required to run online
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOnline(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getApiKey())) {
			throw new SwitcherContextException("API Key not defined [add: switcher.apikey]");
		}
		
		if (StringUtils.isBlank(prop.getDomain())) {
			throw new SwitcherContextException("Domain not defined [add: switcher.domain]");
		}
		
		if (StringUtils.isBlank(prop.getComponent())) {
			throw new SwitcherContextException("Component not defined [add: switcher.component]");
		}
	}
}
