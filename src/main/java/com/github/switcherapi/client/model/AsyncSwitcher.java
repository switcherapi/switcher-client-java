package com.github.switcherapi.client.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * Implementation handle asynchronous Criteria execution when using throttle.
 * <br>Threads are only created when the time calculated for the next run is lower than the current time.
 * 
 * @author Roger Floriano (petruki)
 * @since 2021-11-27
 *
 */
public class AsyncSwitcher implements Runnable {

	private static final Logger logger = LogManager.getLogger(AsyncSwitcher.class);

	private Switcher switcher;

	private long nextRun = 0;

	/**
	 * Validate if next run is ready to be performed, otherwise it will skip and delegate the
	 * Switcher result for the Switcher history execution.
	 */
	public synchronized void execute(final Switcher switcher) {
		this.switcher = switcher;
		
		if (logger.isDebugEnabled())
			logger.debug(String.format("nextRun: %s - currentTimeMillis: %s", nextRun,
					System.currentTimeMillis()));
		
		if (nextRun < System.currentTimeMillis()) {
			if (logger.isDebugEnabled())
				logger.debug("Running AsyncSwitcher");

			this.nextRun = System.currentTimeMillis() + switcher.delay;
			new Thread(this, AsyncSwitcher.class.getName()).start();
		}
	}

	@Override
	public void run() {
		final CriteriaResponse response = switcher.getContext().executeCriteria(this.switcher);
		switcher.getHistoryExecution().add(response);
	}

}
