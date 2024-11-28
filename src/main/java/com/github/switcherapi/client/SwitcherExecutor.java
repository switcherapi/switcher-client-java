package com.github.switcherapi.client;

import com.github.switcherapi.client.exception.SwitcherRemoteException;
import com.github.switcherapi.client.exception.SwitcherSnapshotWriteException;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Domain;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.SwitcherResult;
import com.github.switcherapi.client.service.remote.ClientRemote;
import com.github.switcherapi.client.utils.SnapshotLoader;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The Executor provides an API to handle Remote and Local functionalities that
 * should be available for both Services implementations.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public abstract class SwitcherExecutor {
	
	private static final Logger logger = LoggerFactory.getLogger(SwitcherExecutor.class);
	
	private static final Map<String, SwitcherResult> bypass = new HashMap<>();

	protected final SwitcherProperties switcherProperties;

	protected Domain domain;

	protected SwitcherExecutor(final SwitcherProperties switcherProperties) {
		this.switcherProperties = switcherProperties;
	}
	
	/**
	 * Execute criteria based on the Switcher configuration
	 * 
	 * @param switcher to be evaluated
	 * @return Criteria response containing the evaluation details
	 */
	public abstract SwitcherResult executeCriteria(final Switcher switcher);
	
	/**
	 * Check the snapshot versions against the Remote configuration.
	 * 
	 * @return True if snapshot is up-to-date
	 */
	public abstract boolean checkSnapshotVersion();
	
	/**
	 * Retrieve updated snapshot from the remote API
	 */
	public abstract void updateSnapshot();
	
	/**
	 * Check set of Switchers if they are properly configured.
	 * 
	 * @param switchers To be validated
	 */
	public abstract void checkSwitchers(final Set<String> switchers);

	/**
	 * Retrieve local snapshot version
	 *
	 * @return snapshot version
	 */
	public abstract long getSnapshotVersion();
	
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
	
	/**
	 * It manipulates the result of a given Switcher key.
	 * 
	 * @param key name of the key that you want to change the result
	 * @param expectedResult that will be returned when performing isItOn
	 * @return SwitcherResult with the manipulated result
	 */
	public static SwitcherResult assume(final String key, boolean expectedResult) {
		return assume(key, expectedResult, null);
	}

	/**
	 * It manipulates the result of a given Switcher key.
	 *
	 * @param key name of the key that you want to change the result
	 * @param metadata additional information about the assumption (JSON)
	 * @param expectedResult that will be returned when performing isItOn
	 * @return SwitcherResult with the manipulated result
	 */
	public static SwitcherResult assume(final String key, boolean expectedResult, String metadata) {
		SwitcherResult switcherResult =  new SwitcherResult();
		switcherResult.setResult(expectedResult);
		switcherResult.setReason("Switcher bypassed");

		if (StringUtils.isNotBlank(metadata)) {
			Gson gson = new Gson();
			switcherResult.setMetadata(gson.fromJson(metadata, Object.class));
		}

		bypass.put(key, switcherResult);
		return switcherResult;
	}
	
	/**
	 * It will clean up any result manipulation added before by invoking {@link SwitcherExecutor#assume(String, boolean)}
	 * 
	 * @param key name of the key you want to remove
	 */
	public static void forget(final String key) {
		bypass.remove(key);
	}

	public static Map<String, SwitcherResult> getBypass() {
		return bypass;
	}

	public SwitcherProperties getSwitcherProperties() {
		return switcherProperties;
	}

	public Domain getDomain() {
		return domain;
	}

	public void setDomain(Domain domain) {
		this.domain = domain;
	}
}
