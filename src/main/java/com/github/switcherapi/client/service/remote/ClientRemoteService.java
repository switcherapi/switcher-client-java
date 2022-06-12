package com.github.switcherapi.client.service.remote;

import java.util.Date;
import java.util.Set;

import javax.ws.rs.core.Response;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.exception.SwitcherAPIConnectionException;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.exception.SwitcherInvalidDateTimeArgumentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotAvailableForComponentException;
import com.github.switcherapi.client.exception.SwitcherKeyNotFoundException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.AuthResponse;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import com.github.switcherapi.client.model.response.SnapshotVersionResponse;
import com.github.switcherapi.client.utils.SwitcherContextParam;
import com.github.switcherapi.client.utils.SwitcherUtils;
import com.github.switcherapi.client.ws.ClientWS;
import com.github.switcherapi.client.ws.ClientWSImpl;

/**
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 */
public class ClientRemoteService {
	
	private static ClientRemoteService instance;
	
	private ClientWS clientService;
	
	private AuthResponse authResponse;
	
	private ClientRemoteService() {
		this.clientService = new ClientWSImpl();
	}
	
	public static ClientRemoteService getInstance() {
		if (instance == null) {
			instance = new ClientRemoteService();
		}
		return instance;
	}
	
	public CriteriaResponse executeCriteria(final Switcher switcher)  {
		if (!this.isTokenValid()) {
			this.auth();
		}
				
		final Response response = this.clientService.executeCriteriaService(
				switcher, this.authResponse.getToken());
		
		if (response.getStatus() == 401) {
			throw new SwitcherKeyNotAvailableForComponentException(
					SwitcherContext.getProperties().getComponent(), switcher.getSwitcherKey());
		} else if (response.getStatus() != 200) {
			throw new SwitcherKeyNotFoundException(switcher.getSwitcherKey());
		}
		
		final CriteriaResponse criteriaReponse = response.readEntity(CriteriaResponse.class);
		criteriaReponse.setSwitcherKey(switcher.getSwitcherKey());
		criteriaReponse.setEntry(switcher.getEntry());
		response.close();
		return criteriaReponse;
	}
	
	public Snapshot resolveSnapshot() {
		try {
			if (!this.isTokenValid()) {
				this.auth();
			}
					
			final Response response = this.clientService.resolveSnapshot(this.authResponse.getToken());
			final Snapshot snapshot = response.readEntity(Snapshot.class);
			response.close();
			return snapshot;
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl(), e);
		}
	}
	
	public boolean checkSnapshotVersion(final long version) {
		try {
			if (!this.isTokenValid()) {
				this.auth();
			}
					
			final Response response = this.clientService.checkSnapshotVersion(version, this.authResponse.getToken());
			final SnapshotVersionResponse snapshotVersionResponse = response.readEntity(SnapshotVersionResponse.class);
			response.close();
			
			return snapshotVersionResponse.isUpdated();
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl(), e);
		}
	}
	
	public SwitchersCheck checkSwitchers(final Set<String> switchers) {
		try {
			if (!this.isTokenValid()) {
				this.auth();
			}
					
			final Response response = this.clientService.checkSwitchers(switchers, this.authResponse.getToken());
			
			if (response.getStatus() != 200) {
				throw new SwitcherException(
						String.format("API returned an HTTP/1.1 %s", response.getStatus()), null); 
			}
				
			final SwitchersCheck switchersResponse = response.readEntity(SwitchersCheck.class);
			response.close();
			
			return switchersResponse;
		} catch (final Exception e) {
			throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl(), e);
		}
	}
	
	private void auth() {
		try {
			final Response response = this.clientService.auth();
			
			if (response.getStatus() == 401) {
				throw new SwitcherException("Unauthorized API access", null); 
			}
			
			this.authResponse = response.readEntity(AuthResponse.class);
			response.close();
		} catch (final SwitcherException e) {
			throw e;
		} catch (final Exception e) {
			this.setSilentModeExpiration();
			throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl(), e);
		}
	}
	
	private boolean isTokenValid() throws SwitcherAPIConnectionException, 
		SwitcherInvalidDateTimeArgumentException {
		
		if (this.authResponse != null) {
			if (this.authResponse.getToken().equals(SwitcherContextParam.SILENT_MODE) && !this.authResponse.isExpired()) {
				throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl());
			} else {
				if (!this.clientService.isAlive()) {
					this.setSilentModeExpiration();
					throw new SwitcherAPIConnectionException(SwitcherContext.getProperties().getUrl());
				}
				
				return !authResponse.isExpired();
			}
		}
		
		return false;
	}
	
	private void setSilentModeExpiration() throws SwitcherInvalidDateTimeArgumentException {
		if (SwitcherContext.getProperties().isSilentMode()) {
			final String addValue = SwitcherContext.getProperties().getRetryAfter();
			final AuthResponse response = new AuthResponse();
			
			response.setToken(SwitcherContextParam.SILENT_MODE);
			response.setExp(SwitcherUtils.addTimeDuration(addValue, new Date()).getTime()/1000);
			this.authResponse = response;
		}
	}

	public void setClientWS(ClientWS clientService) {
		this.clientService = clientService;
	}

	public void clearAuthResponse() {
		this.authResponse = null;
	}

}
