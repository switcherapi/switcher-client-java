package com.github.switcherapi.client.service.remote;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import com.github.switcherapi.client.utils.SnapshotEventHandler;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherRemoteService extends SwitcherExecutor {
	
	private static final Logger logger = LogManager.getLogger(SwitcherRemoteService.class);
	
	private final SwitcherLocalService switcherOffline;

	private final ClientRemote clientRemote;
	
	public SwitcherRemoteService() {
		this.switcherOffline = new SwitcherLocalService();
		this.clientRemote = new ClientRemoteService();
	}

	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		try {
			final CriteriaResponse response = this.clientRemote.executeCriteria(switcher);
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Online] response: %s", response));
			}
			
			return response;
		} catch (final SwitcherRemoteException e) {
			logger.error("Failed to execute criteria - {}\nCause: {}", e.getMessage(), e.getCause());
			return executeSilentCriteria(switcher, e);
		}
	}
	
	private CriteriaResponse executeSilentCriteria(final Switcher switcher, 
			final SwitcherRemoteException e) {
		if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.SILENT_MODE))) {
			CriteriaResponse response = this.switcherOffline.executeCriteria(switcher);
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Silent] response: %s", response));
			}
			
			return response;
		} else {
			throw e;
		}
	}

	@Override
	public boolean checkSnapshotVersion() {
		if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION))
				&& this.switcherOffline.getDomain() != null) {
			return super.checkSnapshotVersion(this.clientRemote, this.switcherOffline.getDomain());
		}
		
		super.initializeSnapshotFromAPI(this.clientRemote);
		return Boolean.TRUE;
	}

	@Override
	public void updateSnapshot() {
		this.switcherOffline.setDomain(super.initializeSnapshotFromAPI(this.clientRemote));
	}
	
	@Override
	public void checkSwitchers(final Set<String> switchers) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switchers: %s", switchers));
		}
		
		final SwitchersCheck response = this.clientRemote.checkSwitchers(switchers);
		if (response.getNotFound() != null && response.getNotFound().length > 0) {
			throw new SwitchersValidationException(Arrays.toString(response.getNotFound()));
		}
	}
	
	@Override
	public boolean notifyChange(String snapshotFile, SnapshotEventHandler handler) {
		return this.switcherOffline.notifyChange(snapshotFile, handler);
	}

	@Override
	public long getSnapshotVersion() {
		return switcherOffline.getSnapshotVersion();
	}

}
