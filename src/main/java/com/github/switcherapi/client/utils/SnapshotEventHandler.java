package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.exception.SwitcherException;
import org.slf4j.LoggerFactory;

/**
 * Access snapshot event handler when a file is modified.<br>
 * In case of error, in-memory snapshot won't be replaced.
 * 
 * @author Roger Floriano (petruki)
 * @since 2022-06-29
 */
public interface SnapshotEventHandler {

	/**
	 * Callback method that will be invoked when the snapshot is updated
	 */
	default void onSuccess() {
		SwitcherUtils.debug(LoggerFactory.getLogger(SnapshotEventHandler.class), "Snapshot has been changed");
	}

	/**
	 * Callback method that will be invoked when the snapshot update fails
	 *
	 * @param exception Exception
	 */
	default void onError(SwitcherException exception) {
		LoggerFactory.getLogger(SnapshotEventHandler.class).error(exception.getMessage(), exception);
	}

}
