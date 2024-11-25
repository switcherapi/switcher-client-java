package com.github.switcherapi.client.service.local;

import static com.github.switcherapi.client.remote.Constants.DEFAULT_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.switcherapi.client.SwitcherProperties;
import com.github.switcherapi.client.remote.ClientWS;
import com.github.switcherapi.client.remote.ClientWSImpl;
import com.github.switcherapi.client.service.SwitcherValidator;
import com.github.switcherapi.client.service.ValidatorService;
import com.github.switcherapi.client.service.remote.ClientRemoteService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.utils.SnapshotEventHandler;

class SwitcherLocalServiceTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";

	private static ExecutorService executorService;
	
	private static SwitcherLocalService service;
	
	@BeforeAll
	static void init() {
		executorService = Executors.newSingleThreadExecutor();
		SwitchersBase.configure(ContextBuilder.builder()
				.context("com.github.switcherapi.SwitchersBase")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.environment("default")
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
	void shouldNotifyWithSuccess() {
		SwitchersBase.configure(ContextBuilder.builder()
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
