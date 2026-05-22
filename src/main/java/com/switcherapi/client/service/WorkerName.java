package com.switcherapi.client.service;

public enum WorkerName {

    REGEX_VALIDATOR_WORKER("switcherapi-regex-validator"),
    SNAPSHOT_WATCH_WORKER("switcherapi-snapshot-watcher"),
    SNAPSHOT_UPDATE_WORKER("switcherapi-snapshot-update"),
    SWITCHER_REMOTE_WORKER("switcherapi-remote-pool"),
    SWITCHER_ASYNC_WORKER("switcherapi-async"),
    SWITCHER_TOKEN_WORKER("switcherapi-token-refresh");

    private final String name;

    WorkerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
