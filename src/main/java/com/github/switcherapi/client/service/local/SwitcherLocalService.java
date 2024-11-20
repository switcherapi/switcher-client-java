package com.github.switcherapi.client.service.local;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.*;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.utils.SnapshotEventHandler;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherLocalService extends SwitcherExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(SwitcherLocalService.class);

	private final ClientRemote clientRemote;

	private final ClientLocal clientLocal;
	
	public SwitcherLocalService(ClientRemote clientRemote, ClientLocal clientLocal) {
		this.clientRemote = clientRemote;
		this.clientLocal = clientLocal;
		this.init();
	}
	
	/**
	 * Initialize snapshot in memory. It prioritizes direct file path over environment based snapshot
	 * 
	 * @throws SwitcherSnapshotLoadException in case it was not possible to load snapshot automatically
	 */
	public void init() {
		final String snapshotLocation = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION);
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		final boolean snapshotAutoload = SwitcherContextBase.contextBol(ContextKey.SNAPSHOT_AUTO_LOAD);
		
		if (StringUtils.isBlank(snapshotLocation) && snapshotAutoload) {
			this.domain = this.initializeSnapshotFromAPI(this.clientRemote);
		} else if (StringUtils.isNotBlank(snapshotLocation)) {
			try {
				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
			} catch (IOException e) {
				if (snapshotAutoload) {
					this.domain = this.initializeSnapshotFromAPI(this.clientRemote);
				}
			}
		}
	}

	/**
	 * Update in-memory snapshot.
	 *
	 * @param snapshotFile Path location
	 * @param handler to notify snapshot change events
	 *
	 * @return true if valid change
	 */
	public boolean notifyChange(final String snapshotFile, SnapshotEventHandler handler) {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		final String snapshotLocation = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION);

		try {
			if (snapshotFile.equals(String.format("%s.json", environment))) {
				SwitcherUtils.debug(logger, "Updating domain");

				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
				handler.onSuccess();
			}
		} catch (SwitcherSnapshotLoadException | IOException e) {
			handler.onError(new SwitcherException(e.getMessage(), e));
			logger.error(e.getMessage(), e);
			return false;
		}

		return true;
	}

	/**
	 * Update in-memory snapshot.
	 *
	 * @param snapshotFile Path location
	 * @return true if valid change
	 */
	public boolean notifyChange(final String snapshotFile) {
		return this.notifyChange(snapshotFile, new SnapshotEventHandler() {});
	}
	
	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		SwitcherUtils.debug(logger, "[Local] request: {}", switcher);

		CriteriaResponse response;
		try {
			if (switcher.isRemote()) {
				response = this.clientRemote.executeCriteria(switcher);
				SwitcherUtils.debug(logger, "[Remote] response: {}", response);
			} else {
				response = this.clientLocal.executeCriteria(switcher, this.domain);
				SwitcherUtils.debug(logger, "[Local] response: {}", response);
			}
		} catch (SwitcherKeyNotFoundException e) {
			if (StringUtils.isBlank(switcher.getDefaultResult())) {
				throw e;
			}

			response = CriteriaResponse.buildFromDefault(switcher);
			SwitcherUtils.debug(logger, "[Default] response: {}", response);
		}
		
		return response;
	}
	
	@Override
	public boolean checkSnapshotVersion() {
		return super.checkSnapshotVersion(this.clientRemote, this.domain);
	}

	@Override
	public void updateSnapshot() {
		this.domain = super.initializeSnapshotFromAPI(this.clientRemote);
	}
	
	@Override
	public void checkSwitchers(final Set<String> switchers) {
		SwitcherUtils.debug(logger, "switchers: {}", switchers);
		
		final List<String> response = this.clientLocal.checkSwitchers(switchers, this.domain);
		if (!response.isEmpty()) {
			throw new SwitchersValidationException(response.toString());
		}
	}

	@Override
	public long getSnapshotVersion() {
		return domain.getVersion();
	}

}
