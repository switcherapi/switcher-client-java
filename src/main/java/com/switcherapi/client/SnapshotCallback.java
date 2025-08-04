package com.switcherapi.client;

import com.switcherapi.client.utils.SwitcherUtils;
import org.slf4j.LoggerFactory;

public interface SnapshotCallback {

    /**
     * Callback method that will be invoked when the snapshot is updated
     *
     * @param version of the new snapshot
     */
    default void onSnapshotUpdate(long version) {
        SwitcherUtils.debug(LoggerFactory.getLogger(SnapshotCallback.class), "Snapshot updated: {}", version);
    }

    /**
     * Callback method that will be invoked when the snapshot update fails
     *
     * @param e Exception
     */
    default void onSnapshotUpdateError(Exception e) {
        SwitcherUtils.debug(LoggerFactory.getLogger(SnapshotCallback.class), "Failed to update snapshot: {}", e.getMessage());
    }
}
