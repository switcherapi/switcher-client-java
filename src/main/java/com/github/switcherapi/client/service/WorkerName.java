package com.github.switcherapi.client.service;

public enum WorkerName {

    REGEX_VALIDATOR_WORKER("switcherapi-regex-validator"),
    SNAPSHOT_WATCH_WORKER("switcherapi-snapshot-watcher");

    private final String name;

    WorkerName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
