package com.switcher.client.service;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.switcher.client.domain.AuthRequest;
import com.switcher.client.domain.AuthResponse;
import com.switcher.client.domain.Switcher;
import com.switcher.client.utils.SwitcherContextParam;

public class ClientServiceImpl implements ClientService {
	
	final static Logger logger = Logger.getLogger(ClientServiceImpl.class);
	
	private Client client;
	
	public ClientServiceImpl() {
		this.setClient(ClientBuilder.newClient());
	}
	
	public Response executeCriteriaService(final Map<String, Object> properties, 
			final Switcher switcher) throws Exception {
		
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("switcher: %s", switcher));
		}
		
		final WebTarget myResource = client.target((String) properties.get(SwitcherContextParam.URL))
				.queryParam(Switcher.KEY, switcher.getKey())
				.queryParam(Switcher.SHOW_REASON, Boolean.TRUE)
				.queryParam(Switcher.BYPASS_METRIC, properties.containsKey(Switcher.BYPASS_METRIC) ? 
						properties.get(Switcher.BYPASS_METRIC) : false);
		
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
			.header(HEADER_AUTHORIZATION, String.format(TOKEN_TEXT, ((AuthResponse) properties.get(AUTH_RESPONSE)).getToken()))
			.post(Entity.json(switcher.getInputRequest()));
		
		return response;
	}
	
	public Response auth(final Map<String, Object> properties) throws Exception {
		
		final AuthRequest authRequest = new AuthRequest();
		authRequest.setDomain((String) properties.get(SwitcherContextParam.DOMAIN));
		authRequest.setComponent((String) properties.get(SwitcherContextParam.COMPONENT));
		authRequest.setEnvironment((String) properties.get(SwitcherContextParam.ENVIRONMENT));

		final WebTarget myResource = client.target(String.format(AUTH_URL, properties.get(SwitcherContextParam.URL)));
		final Response response = myResource.request(MediaType.APPLICATION_JSON)
			.header(HEADER_APIKEY, properties.get(SwitcherContextParam.APIKEY))
			.post(Entity.json(authRequest));	

		return response;
	}

	public void setClient(Client client) {
		
		this.client = client;
	}

}
