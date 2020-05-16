package com.github.petruki.switcher.client.service;

import java.util.Map;

import javax.ws.rs.core.Response;

import com.github.petruki.switcher.client.model.Switcher;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public interface ClientService {
	
	String AUTH_RESPONSE = "authResponse";
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
	 * Returns the verification configured for a specific switcher (key)
	 * 
	 * @param properties store all necessary data to build the context
	 * @param switcher store all necessary input to access the criteria
	 * @return the execution based on the configured switcher
	 */
	public Response executeCriteriaService(final Map<String, Object> properties, 
			final Switcher switcher);
	
	/**
	 * Returns the token to access all available endpoints
	 * 
	 * @param properties store all necessary data to build the context
	 * @return token and expiration date
	 */
	public Response auth(final Map<String, Object> properties);
	
	/**
	 * Returns the whole domain structure which will be stored into a snapshot file
	 * 
	 * @param properties store all necessary data to build the context
	 * @return domain structure
	 */
	public Response resolveSnapshot(final Map<String, Object> properties);
	
	/**
	 * Returns { status: true } if snapshot is updated
	 * 
	 * @param properties store all necessary data to build the context
	 * @param version current domain version
	 * @return status: true if domain is updated
	 */
	public Response checkSnapshotVersion(final Map<String, Object> properties, final long version);

}
