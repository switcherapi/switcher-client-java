package com.github.switcherapi.client;

import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.logging.log4j.LogManager;

public interface SnapshotCallback {

    /**
     * Callback method that will be invoked when the snapshot is updated
     *
     * @param version of the new snapshot
     */
    default void onSnapshotUpdate(long version) {
        SwitcherUtils.debug(LogManager.getLogger(SnapshotCallback.class), "Snapshot updated: {}", version);
    }

    /**
     * Callback method that will be invoked when the snapshot update fails
     *
     * @param e Exception
     */
    default void onSnapshotUpdateError(Exception e) {
        SwitcherUtils.debug(LogManager.getLogger(SnapshotCallback.class), "Failed to update snapshot: {}", e.getMessage());
    }
}
