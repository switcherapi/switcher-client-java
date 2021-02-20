package com.github.switcherapi.client.ws;

import javax.ws.rs.core.Response;

import com.github.switcherapi.client.model.Switcher;

/**
 * @author Roger Floriano {petruki)
 * @since 2019-12-24
 */
public interface ClientWS {
	
	String HEADER_AUTHORIZATION = "Authorization";
	String HEADER_APIKEY = "switcher-api-key";
	String TOKEN_TEXT = "Bearer %s";
	
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
	 * Returns the verification configured for a specific switcher (key)
	 * 
	 * @param switcher store all necessary input to access the criteria
	 * @return the execution based on the configured switcher
	 */
	public Response executeCriteriaService(final Switcher switcher, final String token);
	
	/**
	 * Returns the token to access all available endpoints
	 * 
	 * @return token and expiration date
	 */
	public Response auth();
	
	/**
	 * Returns the whole domain structure which will be stored into a snapshot file
	 * 
	 * @return domain structure
	 */
	public Response resolveSnapshot(final String token);
	
	/**
	 * Returns { status: true } if snapshot is updated
	 * 
	 * @param version current domain version
	 * @return status: true if domain is updated
	 */
	public Response checkSnapshotVersion(final long version, final String token);
	
	/**
	 * Check whether API is online or not
	 */
	public boolean isAlive();
}
