package com.github.switcherapi.client.service.remote;

import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitchersValidationException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.remote.dto.SwitchersCheck;
import com.github.switcherapi.client.remote.dto.CriteriaRequest;
import com.github.switcherapi.client.model.SwitcherResult;
import com.github.switcherapi.client.remote.dto.CriteriaResponse;
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
		super(switcherExecutor.getSwitcherProperties());
		this.clientRemote = clientRemote;
		this.switcherLocal = switcherExecutor;
	}

	@Override
	public SwitcherResult executeCriteria(final Switcher switcher) {
		SwitcherUtils.debug(logger, "[Remote] request: {}", switcher);
		
		try {
			final CriteriaResponse response = this.clientRemote.executeCriteria(CriteriaRequest.build(switcher));
			SwitcherUtils.debug(logger, "[Remote] response: {}", response);
			
			return SwitcherResult.buildResultFromRemote(response);
		} catch (final SwitcherRemoteException e) {
			logger.error("Failed to execute criteria - Cause: {}", e.getMessage(), e.getCause());
			return tryExecuteLocalCriteria(switcher, e);
		}
	}
	
	private SwitcherResult tryExecuteLocalCriteria(final Switcher switcher,
												   final SwitcherRemoteException e) {
		if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.SILENT_MODE))) {
			final SwitcherResult response = this.switcherLocal.executeCriteria(switcher);
			SwitcherUtils.debug(logger, "[Silent] response: {}", response);

			return response;
		}

		if (StringUtils.isNotBlank(switcher.getDefaultResult())) {
			final SwitcherResult response = SwitcherResult.buildFromDefault(switcher);
			SwitcherUtils.debug(logger, "[Default] response: {}", response);

			return response;
		}

		throw e;
	}

	@Override
	public boolean checkSnapshotVersion() {
		if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.SNAPSHOT_LOCATION))
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
