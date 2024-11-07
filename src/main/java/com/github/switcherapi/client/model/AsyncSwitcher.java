package com.github.switcherapi.client.model;

import com.github.switcherapi.client.utils.SwitcherUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

	private final ExecutorService executorService;

	private final Switcher switcher;

	private long nextRun = 0;

	public AsyncSwitcher(final Switcher switcher) {
		this.executorService = Executors.newCachedThreadPool();
		this.switcher = switcher;
	}

	/**
	 * Validate if next run is ready to be performed, otherwise it will skip and delegate the
	 * Switcher result for the Switcher history execution.
	 */
	public synchronized void execute() {
		SwitcherUtils.debug(logger, "nextRun: {} - currentTimeMillis: {}", nextRun, System.currentTimeMillis());
		
		if (nextRun < System.currentTimeMillis()) {
			SwitcherUtils.debug(logger, "Running AsyncSwitcher");

			this.nextRun = System.currentTimeMillis() + switcher.delay;
			this.executorService.submit(this);
		}
	}

	@Override
	public void run() {
		try {
			final CriteriaResponse response = switcher.getContext().executeCriteria(switcher);
			switcher.getHistoryExecution().removeIf(item ->
					switcher.getSwitcherKey().equals(item.getSwitcherKey()) &&
					switcher.getEntry().equals(item.getEntry()));
			switcher.getHistoryExecution().add(response);
		} catch (SwitcherException e) {
			logger.error(e);
		}
	}

}
