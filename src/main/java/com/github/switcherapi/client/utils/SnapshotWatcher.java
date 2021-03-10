package com.github.switcherapi.client.utils;

import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.factory.SwitcherExecutor;

/**
 * @author Roger Floriano (petruki)
 * @since 2020-05-13
 */
public class SnapshotWatcher implements Runnable {
	
	private static final Logger logger = LogManager.getLogger(SnapshotWatcher.class);
	
	private WatchService watcher;
	
	private SwitcherExecutor executorInstance;
	
	public SnapshotWatcher(final SwitcherExecutor executorInstance) {
		
		this.executorInstance = executorInstance;
	}

	@Override
	public void run() {
		
		WatchKey key;
		
		try {
			watcher = FileSystems.getDefault().newWatchService();
			final Path dir = Paths.get(SwitcherContext.getProperties().getSnapshotLocation());
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
		    		
	    			logger.debug("Snapshot has been changed");
		    		executorInstance.notifyChange(filename.toString());
		    	}
			    
			    boolean valid = key.reset();
			    
			    if (!valid)
			        break;
		    }
		} catch (IOException | InterruptedException | ClosedWatchServiceException e) {
			Thread.currentThread().interrupt();
			this.executorInstance = null;
		}
	}
	
	public void terminate() {
		try {
			watcher.close();
		} catch (IOException e) {
			logger.error(e);
		}
	}

}
