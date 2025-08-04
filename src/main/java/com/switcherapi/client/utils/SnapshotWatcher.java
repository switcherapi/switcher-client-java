package com.switcherapi.client.utils;

import com.switcherapi.client.service.local.SwitcherLocalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

/**
 * SnapshotWatcher runs in a separate thread to watch for changes in the snapshot file.
 *
 * @author Roger Floriano (petruki)
 * @since 2020-05-13
 */
public class SnapshotWatcher implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(SnapshotWatcher.class);
	
	private final SnapshotEventHandler handler;

	private final String snapshotLocation;
	
	private WatchService watcher;
	
	private SwitcherLocalService executorInstance;
	
	public SnapshotWatcher(final SwitcherLocalService executorInstance,
						   final SnapshotEventHandler handler,
						   final String snapshotLocation) {
		this.executorInstance = executorInstance;
		this.handler = handler;
		this.snapshotLocation = snapshotLocation;
	}

	@Override
	public void run() {
		WatchKey key;
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
			final Path dir = Paths.get(snapshotLocation);
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
			logger.error(e.getMessage(), e);
		}
	}

}
