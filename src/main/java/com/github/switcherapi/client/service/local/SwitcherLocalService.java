package com.github.switcherapi.client.service.local;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherSnapshotLoadException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.utils.SnapshotEventHandler;
import com.github.switcherapi.client.utils.SnapshotLoader;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherLocalService extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherLocalService.class);
	
	private Domain domain;
	
	public SwitcherLocalService() {
		this.init();
	}
	
	/**
	 * Initialize snapshot in memory. It prioritizes direct file path over environment based snapshot
	 * 
	 * @throws SwitcherSnapshotLoadException in case it was not possible to load snapshot automatically
	 */
	public void init() {
		final String snapshotFile = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_FILE);
		final String snapshotLocation = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION);
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		final boolean snapshotAutoload = SwitcherContextBase.contextBol(ContextKey.SNAPSHOT_AUTO_LOAD);
		
		if (StringUtils.isNotBlank(snapshotFile)) {
			this.domain = SnapshotLoader.loadSnapshot(snapshotFile);
		} else if (StringUtils.isNotBlank(snapshotLocation)) {
			try {
				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
			} catch (FileNotFoundException e) {
				if (snapshotAutoload) {
					this.domain = this.initializeSnapshotFromAPI();
				}
			}
		}
	}
	
	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final CriteriaResponse response = ClientLocalService.getInstance().executeCriteria(switcher, this.domain);
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("[Offline] response: %s", response));
		}
		
		return response;
	}
	
	
	@Override
	public boolean checkSnapshotVersion() {
		return super.checkSnapshotVersion(this.domain);
	}

	@Override
	public void updateSnapshot() {
		this.domain = super.initializeSnapshotFromAPI();
	}
	
	@Override
	public void checkSwitchers(final Set<String> switchers) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switchers: %s", switchers));
		}
		
		final List<String> response = ClientLocalService.getInstance().checkSwitchers(switchers, this.domain);
		if (!response.isEmpty()) {
			throw new SwitchersValidationException(response.toString());
		}
	}
	
	@Override
	public boolean notifyChange(final String snapshotFile, SnapshotEventHandler handler) {
		final String environment = SwitcherContextBase.contextStr(ContextKey.ENVIRONMENT);
		final String snapshotLocation = SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION);
		
		try {
			if (snapshotFile.equals(String.format("%s.json", environment))) {
				logger.debug("Updating domain");
				this.domain = SnapshotLoader.loadSnapshot(snapshotLocation, environment);
				handler.onSuccess();
			}
		} catch (SwitcherSnapshotLoadException | FileNotFoundException e) {
			handler.onError(new SwitcherException(e.getMessage(), e));
			logger.error(e);
			return false;
		}
		
		return true;
	}
	
	public Domain getDomain() {
		return domain;
	}

}
