package com.switcherapi.client.service.remote;

import com.switcherapi.client.SwitcherExecutor;
import com.switcherapi.client.SwitcherExecutorImpl;
import com.switcherapi.client.exception.SwitcherRemoteException;
import com.switcherapi.client.exception.SwitchersValidationException;
import com.switcherapi.client.model.ContextKey;
import com.switcherapi.client.model.SwitcherRequest;
import com.switcherapi.client.model.SwitcherResult;
import com.switcherapi.client.remote.dto.CriteriaResponse;
import com.switcherapi.client.remote.dto.SwitchersCheck;
import com.switcherapi.client.service.SwitcherFactory;
import com.switcherapi.client.utils.Mapper;
import com.switcherapi.client.utils.SwitcherUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class SwitcherRemoteService extends SwitcherExecutorImpl {
	
	private static final Logger logger = LoggerFactory.getLogger(SwitcherRemoteService.class);
	
	private final SwitcherExecutor switcherLocal;

	private final ClientRemote clientRemote;
	
	public SwitcherRemoteService(final ClientRemote clientRemote, final SwitcherExecutor switcherExecutor) {
		super(switcherExecutor.getSwitcherProperties());
		this.clientRemote = clientRemote;
		this.switcherLocal = switcherExecutor;
	}

	@Override
	public SwitcherResult executeCriteria(final SwitcherRequest switcher) {
		SwitcherUtils.debug(logger, "[Remote] request: {}", switcher);
		
		try {
			final CriteriaResponse response = this.clientRemote.executeCriteria(Mapper.mapFrom(switcher));
			SwitcherUtils.debug(logger, "[Remote] response: {}", response);
			
			return Mapper.mapFrom(response);
		} catch (final SwitcherRemoteException e) {
			logger.error("Failed to execute criteria - Cause: {}", e.getMessage(), e.getCause());
			return tryExecuteLocalCriteria(switcher, e);
		}
	}

	private SwitcherResult tryExecuteLocalCriteria(final SwitcherRequest switcher,
												   final SwitcherRemoteException e) {
		if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.SILENT_MODE))) {
			final SwitcherResult response = this.switcherLocal.executeCriteria(switcher);
			SwitcherUtils.debug(logger, "[Silent] response: {}", response);

			return response;
		}

		if (StringUtils.isNotBlank(switcher.getDefaultResult())) {
			final SwitcherResult response = SwitcherFactory.buildFromDefault(switcher);
			SwitcherUtils.debug(logger, "[Default] response: {}", response);

			return response;
		}

		throw e;
	}

	@Override
	public boolean checkSnapshotVersion() {
		if (StringUtils.isNotBlank(switcherProperties.getValue(ContextKey.SNAPSHOT_LOCATION))
				&& Objects.nonNull(this.switcherLocal.getDomain())) {
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
