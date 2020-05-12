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
	
	// Endpoints
	public static final String AUTH_URL = "%s/criteria/auth";
	public static final String CRITERIA_URL = "%s/criteria";
	public static final String SNAPSHOT_URL = "%s/graphql";
	
	public Response executeCriteriaService(final Map<String, Object> properties, 
			final Switcher switcher) throws Exception;
	
	public Response auth(final Map<String, Object> properties) throws Exception;
	
	public Response resolveSnapshot(final Map<String, Object> properties) throws Exception;

}
