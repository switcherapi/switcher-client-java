package com.github.switcherapi.client.utils;

import com.github.switcherapi.client.SwitcherContextBase;
import com.github.switcherapi.client.model.ContextKey;
import com.github.switcherapi.client.service.local.SwitcherLocalService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;

/**
 * SnapshotWatcher runs in a separate thread to watch for changes in the snapshot file.
 *
 * @author Roger Floriano (petruki)
 * @since 2020-05-13
 */
public class SnapshotWatcher implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(SnapshotWatcher.class);
	
	private final SnapshotEventHandler handler;
	
	private WatchService watcher;
	
	private SwitcherLocalService executorInstance;
	
	public SnapshotWatcher(final SwitcherLocalService executorInstance, SnapshotEventHandler handler) {
		this.executorInstance = executorInstance;
		this.handler = handler;
	}

	@Override
	public void run() {
		
		WatchKey key;
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
			final Path dir = Paths.get(SwitcherContextBase.contextStr(ContextKey.SNAPSHOT_LOCATION));
			dir.register(watcher,
		    		StandardWatchEventKinds.ENTRY_DELETE,
		    		StandardWatchEventKinds.ENTRY_MODIFY);

		    for (;;) {
			    key = watcher.take();
			    Thread.sleep(1000); // Gap between writing events so it will load just once
			    
		    	for (WatchEvent<?> event: key.pollEvents()) {
		    		@SuppressWarnings("unchecked")
		    		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		    		Path filename = ev.context();
		    		
		    		if (executorInstance != null) {
						executorInstance.notifyChange(filename.toString(), handler);
					}
		    	}

			    if (!key.reset()) {
					break;
				}
		    }
		} catch (IOException | InterruptedException | ClosedWatchServiceException e) {
			Thread.currentThread().interrupt();
			this.executorInstance = null;
		}
	}
	
	public void terminate() {
		try {
			if (watcher != null) {
				watcher.close();
			}
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
