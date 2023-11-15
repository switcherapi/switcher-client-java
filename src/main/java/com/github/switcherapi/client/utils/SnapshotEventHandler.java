package com.github.switcherapi.client.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;

/**
 * Access snapshot event handler when a file is modified.<br>
 * In case of error, in-memory snapshot won't be replaced.
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-29
 */
public class SnapshotEventHandler {
	
	private static final Logger logger = LogManager.getLogger(SnapshotEventHandler.class);
	
	public void onSuccess() {
		logger.debug("Snapshot has been changed");
	}
	
	public void onError(SwitcherException exception) {
		logger.error(exception);
	}

}
