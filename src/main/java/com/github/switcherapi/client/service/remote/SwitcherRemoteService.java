package com.github.switcherapi.client.service.remote;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherRemoteService extends SwitcherExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(SwitcherRemoteService.class);
	
	private final SwitcherExecutor switcherLocal;

	private final ClientRemote clientRemote;
	
	public SwitcherRemoteService(ClientRemote clientRemote, SwitcherExecutor switcherExecutor) {
		this.clientRemote = clientRemote;
		this.switcherLocal = switcherExecutor;
	}

	@Override
	public CriteriaResponse executeCriteria(final Switcher switcher) {
		SwitcherUtils.debug(logger, "[Remote] request: {}", switcher);
		
		try {
			final CriteriaResponse response = this.clientRemote.executeCriteria(switcher);
			SwitcherUtils.debug(logger, "[Remote] response: {}", response);
			
			return response;
		} catch (final SwitcherRemoteException e) {
			logger.error("Failed to execute criteria - Cause: {}", e.getMessage(), e.getCause());
			return tryExecuteLocalCriteria(switcher, e);
		}
	}
	
	private CriteriaResponse tryExecuteLocalCriteria(final Switcher switcher,
													 final SwitcherRemoteException e) {
		if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.SILENT_MODE))) {
			final CriteriaResponse response = this.switcherLocal.executeCriteria(switcher);
			SwitcherUtils.debug(logger, "[Silent] response: {}", response);

			return response;
		}

		if (StringUtils.isNotBlank(switcher.getDefaultResult())) {
			final CriteriaResponse response = CriteriaResponse.buildFromDefault(switcher);
			SwitcherUtils.debug(logger, "[Default] response: {}", response);

			return response;
		}

		throw e;
	}

	@Override
	public boolean checkSnapshotVersion() {
		if (StringUtils.isNotBlank(SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION))
				&& this.switcherLocal.getDomain() != null) {
			return super.checkSnapshotVersion(this.clientRemote, this.switcherLocal.getDomain());
		}
		
		super.initializeSnapshotFromAPI(this.clientRemote);
		return Boolean.TRUE;
	}

	@Override
	public void updateSnapshot() {
		this.switcherLocal.setDomain(super.initializeSnapshotFromAPI(this.clientRemote));
	}
	
	@Override
	public void checkSwitchers(final Set<String> switchers) {
		SwitcherUtils.debug(logger, "switchers: {}", switchers);
		
		final SwitchersCheck response = this.clientRemote.checkSwitchers(switchers);
		if (response.getNotFound() != null && response.getNotFound().length > 0) {
			throw new SwitchersValidationException(Arrays.toString(response.getNotFound()));
		}
	}

	@Override
	public long getSnapshotVersion() {
		return switcherLocal.getSnapshotVersion();
	}

}
