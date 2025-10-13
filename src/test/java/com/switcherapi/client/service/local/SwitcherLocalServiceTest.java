package com.switcherapi.client.service.local;

import static com.switcherapi.client.remote.Constants.DEFAULT_ENV;
import static com.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.switcherapi.client.SwitcherProperties;
import com.switcherapi.client.remote.ClientWS;
import com.switcherapi.client.remote.ClientWSImpl;
import com.switcherapi.client.service.SwitcherValidator;
import com.switcherapi.client.service.ValidatorService;
import com.switcherapi.client.service.remote.ClientRemoteService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.switcherapi.SwitchersBase;
import com.switcherapi.client.ContextBuilder;
import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.utils.SnapshotEventHandler;

class SwitcherLocalServiceTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	private static ExecutorService executorService;
	
	private static SwitcherLocalService service;
	
	@BeforeAll
	static void init() {
		executorService = Executors.newSingleThreadExecutor();
		SwitchersBase.configure(ContextBuilder.builder()
				.context(SwitchersBase.class.getName())
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment(DEFAULT_ENV)
				.local(true));

		SwitcherProperties properties = SwitchersBase.getSwitcherProperties();
		ClientWS clientWS = ClientWSImpl.build(properties, executorService, DEFAULT_TIMEOUT);
		SwitcherValidator validatorService = new ValidatorService();
		service = new SwitcherLocalService(
				new ClientRemoteService(clientWS, properties),
				new ClientLocalService(validatorService), properties);
	}

	@AfterAll
	static void tearDown() {
		executorService.shutdown();
	}

	@Test
	void shouldNotifyWithError() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("defect_default"));
		
		assertFalse(service.notifyChange("defect_default.json"));
	}

	@Test
	void shouldNotifyWithErrorResources() {
		SwitchersBase.configure(ContextBuilder.builder()
				.snapshotLocation("")
				.environment("defect_default"));

		assertFalse(service.notifyChange("defect_default.json"));
	}
	
	@Test
	void shouldNotifyWithSuccess() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("snapshot_watcher"));
		
		assertTrue(service.notifyChange("snapshot_watcher.json"));
	}

	@Test
	void shouldNotifyWithSuccessResources() {
		SwitchersBase.configure(ContextBuilder.builder()
				.snapshotLocation("")
				.environment("snapshot_watcher"));

		assertTrue(service.notifyChange("snapshot_watcher.json"));
	}
	
	@Test
	void shouldNotifyWithSuccess_customHandler() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("snapshot_watcher"));
		
		AtomicBoolean snapshotSuccess = new AtomicBoolean(false);
		assertTrue(service.notifyChange("snapshot_watcher.json", new SnapshotEventHandler() {
			@Override
			public void onSuccess() {
				snapshotSuccess.set(true);
			}
		}));
		
		assertTrue(snapshotSuccess.get());
	}
	
	@Test
	void shouldNotifyWithError_customHandler() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("defect_default"));
		
		AtomicBoolean snapshotError = new AtomicBoolean(false);
		assertFalse(service.notifyChange("defect_default.json", new SnapshotEventHandler() {
			@Override
			public void onError(SwitcherException exception) {
				snapshotError.set(true);
			}
		}));
		
		assertTrue(snapshotError.get());
	}

}
