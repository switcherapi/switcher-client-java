package com.github.switcherapi.client.service;

public enum WorkerName {

    REGEX_VALIDATOR_WORKER("switcherapi-regex-validator"),
    SNAPSHOT_WATCH_WORKER("switcherapi-snapshot-watcher"),
    SNAPSHOT_UPDATE_WORKER("switcherapi-snapshot-update");

    private final String name;

    WorkerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
