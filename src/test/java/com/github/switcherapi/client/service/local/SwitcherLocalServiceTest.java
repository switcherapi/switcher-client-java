package com.github.switcherapi.client.service.local;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.switcherapi.SwitchersBase;
import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.utils.SnapshotEventHandler;

class SwitcherLocalServiceTest {
	
	private static final String SNAPSHOTS_LOCAL = Paths.get(StringUtils.EMPTY).toAbsolutePath() + "/src/test/resources";
	
	private static SwitcherLocalService service;
	
	@BeforeAll
	static void init() {
		SwitchersBase.configure(ContextBuilder.builder()
				.contextLocation("com.github.switcherapi.SwitchersBase")
				.snapshotLocation(SNAPSHOTS_LOCAL)
				.offlineMode(true));

		service = new SwitcherLocalService();
	}

	@Test
	void shouldNotifyWithError() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("defect_default"));
		
		assertFalse(service.notifyChange("defect_default.json", new SnapshotEventHandler()));
	}
	
	@Test
	void shouldNotifyWithSuccess() {
		SwitchersBase.configure(ContextBuilder.builder()
				.environment("snapshot_watcher"));
		
		assertTrue(service.notifyChange("snapshot_watcher.json", new SnapshotEventHandler()));
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
