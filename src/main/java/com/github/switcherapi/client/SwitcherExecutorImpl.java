package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class SwitcherExecutorImpl implements SwitcherExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(SwitcherExecutorImpl.class);

	protected final SwitcherProperties switcherProperties;

	protected Domain domain;

	protected SwitcherExecutorImpl(final SwitcherProperties switcherProperties) {
		this.switcherProperties = switcherProperties;
	}

	protected boolean checkSnapshotVersion(ClientRemote clientRemote, final Domain domain) {
		final String environment = switcherProperties.getValue(ContextKey.ENVIRONMENT);
		SwitcherUtils.debug(logger, "verifying snapshot version - environment: {}", environment);

		return clientRemote.checkSnapshotVersion(domain.getVersion());
	}

	protected Domain initializeSnapshotFromAPI(ClientRemote clientRemote)
			throws SwitcherRemoteException, SwitcherSnapshotWriteException {
		final String environment = switcherProperties.getValue(ContextKey.ENVIRONMENT);
		SwitcherUtils.debug(logger, "initializing snapshot from API - environment: {}", environment);

		final Snapshot snapshot = clientRemote.resolveSnapshot();
		final String snapshotLocation = switcherProperties.getValue(ContextKey.SNAPSHOT_LOCATION);

		if (Objects.nonNull(snapshotLocation)) {
			SnapshotLoader.saveSnapshot(snapshot, snapshotLocation, environment);
		}

		return snapshot.getDomain();
	}

	@Override
	public SwitcherProperties getSwitcherProperties() {
		return switcherProperties;
	}

	@Override
	public Domain getDomain() {
		return domain;
	}

	@Override
	public void setDomain(Domain domain) {
		this.domain = domain;
	}

}
