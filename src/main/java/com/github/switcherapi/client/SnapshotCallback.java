package com.github.switcherapi.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SnapshotCallback {

    private static final Logger logger = LogManager.getLogger(SnapshotCallback.class);

    /**
     * Callback method that will be invoked when the snapshot is updated
     *
     * @param version of the new snapshot
     */
    public void onSnapshotUpdate(long version) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Snapshot updated: %s", version));
        }
    }

    /**
     * Callback method that will be invoked when the snapshot update fails
     *
     * @param e Exception
     */
    public void onSnapshotUpdateError(Exception e) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Failed to update snapshot: %s", e.getMessage()));
        }
    }
}
