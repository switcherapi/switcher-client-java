package com.github.switcherapi.client.remote;

import java.util.Optional;
import java.util.Set;

import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;

/**
 * @author Roger Floriano {petruki)
 * @since 2019-12-24
 */
public interface ClientWS {

	/**
	 * Returns the token to access all available endpoints
	 */
	String AUTH_URL = "%s/criteria/auth";

	/**
	 * Returns the verification configured for a specific switcher (key)
	 */
	String CRITERIA_URL = "%s/criteria";

	/**
	 * Returns the whole domain structure
	 */
	String SNAPSHOT_URL = "%s/graphql";

	/**
	 * Returns { status: true } if snapshot is updated
	 */
	String SNAPSHOT_VERSION_CHECK = "%s/criteria/snapshot_check/%s";

	/**
	 * Return { status: 200 } if alive
	 */
	String CHECK_URL = "%s/check";

	/**
	 * Returns array of switcher keys not found
	 */
	String CHECK_SWITCHERS = "%s/criteria/switchers_check";

	/**
	 * Returns the verification configured for a specific switcher (key)
	 *
	 * @param switcher store all necessary input to access the criteria
	 * @param token Access token
	 * @return the execution based on the configured switcher
	 */
	CriteriaResponse executeCriteriaService(final Switcher switcher, final String token);

	/**
	 * Returns the token to access all available endpoints
	 *
	 * @return token and expiration date
	 */
	Optional<AuthResponse> auth();

	/**
	 * Returns the whole domain structure which will be stored into a snapshot file
	 *
	 * @param token Access token
	 * @return domain structure
	 */
	Snapshot resolveSnapshot(final String token);

	/**
	 * Returns { status: true } if snapshot is updated
	 *
	 * @param version current domain version
	 * @param token Access token
	 * @return status: true if domain is updated
	 */
	SnapshotVersionResponse checkSnapshotVersion(final long version, final String token);

	/**
	 * Returns an empty array of not_found if all switchers passed are properly configured.
	 *
	 * @param switchers to be validated
	 * @param token Access token
	 * @return array of Switchers Key not found/configured
	 */
	SwitchersCheck checkSwitchers(final Set<String> switchers, final String token);

	/**
	 * @return Check whether API is remotely available or not
	 */
	boolean isAlive();
}
