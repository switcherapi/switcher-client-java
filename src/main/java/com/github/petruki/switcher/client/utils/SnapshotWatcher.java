package com.github.petruki.switcher.client.utils;

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

import com.github.petruki.switcher.client.factory.SwitcherExecutor;

/**
 * @author rogerio
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
		
		try {
			
			watcher = FileSystems.getDefault().newWatchService();
			final Path dir = Paths.get(executorInstance.getSnapshotLocation());
			WatchKey key = dir.register(watcher,
		    		StandardWatchEventKinds.ENTRY_DELETE,
		    		StandardWatchEventKinds.ENTRY_MODIFY);

		    for (;;) {
		    	
			    key = watcher.take();
			    Thread.sleep(1000); // Gap between writing events so it will load just once
			    
		    	for (WatchEvent<?> event: key.pollEvents()) {
		    		
		    		WatchEvent.Kind<?> kind = event.kind();
		    		
		    		if (kind == StandardWatchEventKinds.OVERFLOW) 
		    			continue;
		    		
		    		@SuppressWarnings("unchecked")
		    		WatchEvent<Path> ev = (WatchEvent<Path>) event;
		    		Path filename = ev.context();
		    		
	    			logger.debug(String.format("File %s has been changed", filename.toString()));
		    		executorInstance.notifyChange(filename.toString());
		    		break;
		    	}
			    
			    boolean valid = key.reset();
			    
			    if (!valid)
			        break;
		    }
		} catch (IOException | InterruptedException e) {
			logger.error(e);
		} catch (ClosedWatchServiceException e) {
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
