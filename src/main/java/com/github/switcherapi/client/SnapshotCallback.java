package com.github.switcherapi.client;

import com.github.switcherapi.client.utils.SwitcherUtils;
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
        SwitcherUtils.debug(logger, "Snapshot updated: {}", version);
    }

    /**
     * Callback method that will be invoked when the snapshot update fails
     *
     * @param e Exception
     */
    public void onSnapshotUpdateError(Exception e) {
        SwitcherUtils.debug(logger, "Failed to update snapshot: {}", e.getMessage());
    }
}
