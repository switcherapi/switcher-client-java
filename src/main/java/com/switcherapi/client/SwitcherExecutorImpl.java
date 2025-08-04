package com.switcherapi.client;

import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.switcherapi.client.model.ContextKey;
import com.switcherapi.client.model.criteria.Domain;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.service.remote.ClientRemote;
import com.switcherapi.client.utils.SnapshotLoader;
import com.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		if (StringUtils.isNotBlank(snapshotLocation)) {
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
