package com.github.switcherapi.client.utils;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherContextException;
import com.github.switcherapi.client.model.SwitcherProperties;

/**
 * Utilitary class to validate SwitcherProperties during initialization
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-17
 *
 */
public class SwitcherContextUtils {
	
	private SwitcherContextUtils() {}

	/**
	 * Validate context properties before executing any Switcher operation
	 * 
	 * @param prop Configured properties
	 * @throws SwitcherContextException if validation fails
	 */
	public static void validate(final SwitcherProperties prop) {
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
			error.append("SwitcherContextParam.SNAPSHOT_FILE or SNAPSHOT_LOCATION not found");
		} else {
			final File file = new File(prop.getSnapshotFile());
			if (!file.exists()) {
				throw new SwitcherContextException("SwitcherContextParam.SNAPSHOT_FILE has invalid file");
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
				throw new SwitcherContextException("SwitcherContextParam.SNAPSHOT_LOCATION has invalid location");
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
			throw new SwitcherContextException("SwitcherContextParam.SNAPSHOT_LOCATION not found");
		}
		
		if (prop.isSilentMode() && StringUtils.isBlank(prop.getRetryAfter())) {
			throw new SwitcherContextException("SwitcherContextParam.RETRY_AFTER not found");
		}
	}

	/**
	 * Validate context properties required to run online
	 * 
	 * @param prop Configured properties
	 */
	public static void validateOnline(final SwitcherProperties prop) {
		if (StringUtils.isBlank(prop.getApiKey())) {
			throw new SwitcherContextException("SwitcherContextParam.APIKEY not found");
		}
		
		if (StringUtils.isBlank(prop.getDomain())) {
			throw new SwitcherContextException("SwitcherContextParam.DOMAIN not found");
		}
		
		if (StringUtils.isBlank(prop.getComponent())) {
			throw new SwitcherContextException("SwitcherContextParam.COMPONENT not found");
		}
	}
}
