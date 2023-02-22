package com.github.switcherapi.client.service.remote;

import java.util.Arrays;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
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
	
	public SwitcherRemoteService() {
		this.switcherOffline = new SwitcherLocalService();
	}

	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		try {
			final CriteriaResponse response = ClientRemoteService.getInstance().executeCriteria(switcher);
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("[Online] response: %s", response));
			}
			
			return response;
		} catch (final SwitcherAPIConnectionException e) {
			logger.error("Failed to execute criteria - {}", e.getMessage());
			return executeSilentCriteria(switcher, e);
		}
	}
	
	private CriteriaResponse executeSilentCriteria(final Switcher switcher, 
			final SwitcherAPIConnectionException e) {
		if (SwitcherContextBase.contextBol(ContextKey.SILENT_MODE)) {
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
			return super.checkSnapshotVersion(this.switcherOffline.getDomain());
		}
		
		super.initializeSnapshotFromAPI();
		return Boolean.TRUE;
	}

	@Override
	public void updateSnapshot() {
		this.switcherOffline.setDomain(super.initializeSnapshotFromAPI());
	}
	
	@Override
	public void checkSwitchers(final Set<String> switchers) {
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switchers: %s", switchers));
		}
		
		final SwitchersCheck response = ClientRemoteService.getInstance().checkSwitchers(switchers);
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
