package com.switcherapi.client.service;

public enum WorkerName {

    SNAPSHOT_WATCH_WORKER("switcherapi-snapshot-watcher"),
    SNAPSHOT_UPDATE_WORKER("switcherapi-snapshot-update"),
    SWITCHER_REMOTE_WORKER("switcherapi-remote-pool"),
    SWITCHER_ASYNC_WORKER("switcherapi-async");

    private final String name;

    WorkerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
