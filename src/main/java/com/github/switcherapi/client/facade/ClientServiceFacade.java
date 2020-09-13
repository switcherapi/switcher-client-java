package com.github.switcherapi.client.facade;

import java.util.Date;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;
import com.github.switcherapi.client.service.ClientService;
import com.github.switcherapi.client.service.ClientServiceImpl;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;

/**
 * @author rogerio
 * @since 2019-12-24
 */
public class ClientServiceFacade {
	
	private static ClientServiceFacade instance;
	
	private ClientService clientService;
	
	private ClientServiceFacade() {
		
		this.clientService = new ClientServiceImpl();
	}
	
	public static ClientServiceFacade getInstance() {
		
		if (instance == null) {
			instance = new ClientServiceFacade();
		}
		return instance;
	}
	
	public CriteriaResponse executeCriteria(final Map<String, Object> properties, final Switcher switcher) 
			throws SwitcherException {
		
		try {
			if (!this.isTokenValid(properties)) {
				this.auth(properties);
			}
					
			final Response response = this.clientService.executeCriteriaService(properties, switcher);
			
			if (response.getStatus() == 401) {
				throw new SwitcherKeyNotAvailableForComponentException(
						properties.get(SwitcherContextParam.COMPONENT).toString(), switcher.getSwitcherKey());
			} else if (response.getStatus() != 200) {
				throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
			}
			
			final CriteriaResponse criteriaReponse = response.readEntity(CriteriaResponse.class);
			criteriaReponse.setSwitcherKey(switcher.getSwitcherKey());
			response.close();
			return criteriaReponse;
		} catch (final SwitcherException e) {
			throw e;
		}
		
	}
	
	public Snapshot resolveSnapshot(final Map<String, Object> properties) 
			throws SwitcherException {
		
		try {
			if (!this.isTokenValid(properties)) {
				this.auth(properties);
			}
					
			final Response response = this.clientService.resolveSnapshot(properties);
			
			final Snapshot snapshot = response.readEntity(Snapshot.class);
			response.close();
			return snapshot;
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
					(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY, e);
		}
		
	}
	
	public boolean checkSnapshotVersion(final Map<String, Object> properties, final long version)
			throws SwitcherException {
		
		try {
			if (!this.isTokenValid(properties)) {
				this.auth(properties);
			}
					
			final Response response = this.clientService.checkSnapshotVersion(properties, version);
			
			final SnapshotVersionResponse snapshotVersionResponse = response.readEntity(SnapshotVersionResponse.class);
			response.close();
			
			return snapshotVersionResponse.isUpdated();
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
					(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY, e);
		}
		
	}
	
	private void auth(final Map<String, Object> properties) throws SwitcherException {
		try {
			final Response response = this.clientService.auth(properties);
			
			if (response.getStatus() == 401) {
				throw new SwitcherException("Unauthorized API access", null); 
			}
			
			final AuthResponse authResponse = response.readEntity(AuthResponse.class);
			properties.put(ClientService.AUTH_RESPONSE, authResponse);
			response.close();
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			this.setSilentModeExpiration(properties);
			throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
					(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY, e);
		}
	}
	
	private boolean isTokenValid(final Map<String, Object> properties) 
			throws SwitcherAPIConnectionException, SwitcherInvalidDateTimeArgumentException {
		
		if (properties.containsKey(ClientService.AUTH_RESPONSE)) {
			final AuthResponse authResponse = (AuthResponse) properties.get(ClientService.AUTH_RESPONSE);
			
			if (authResponse.getToken().equals(SwitcherContextParam.SILENT_MODE)) {
				if (!authResponse.isExpired()) {
					throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
							(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY);
				}
			} else {
				if (!this.clientService.isAlive(properties)) {
					this.setSilentModeExpiration(properties);
					throw new SwitcherAPIConnectionException(properties.containsKey(SwitcherContextParam.URL) ? 
							(String) properties.get(SwitcherContextParam.URL) : StringUtils.EMPTY);
				}
				
				return !authResponse.isExpired();
			}
		}
		
		return false;
	}
	
	private void setSilentModeExpiration(final Map<String, Object> properties) 
			throws SwitcherInvalidDateTimeArgumentException {
		
		if (properties.containsKey(SwitcherContextParam.SILENT_MODE) &&
				(boolean) properties.get(SwitcherContextParam.SILENT_MODE)) {
			
			final String addValue = (String) properties.get(SwitcherContextParam.RETRY_AFTER);
			
			final AuthResponse authResponse = new AuthResponse();
			authResponse.setToken(SwitcherContextParam.SILENT_MODE);
			authResponse.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			properties.put(ClientService.AUTH_RESPONSE, authResponse);
		}
		
	}

	public void setClientService(ClientService clientService) {
		
		this.clientService = clientService;
	}

}
