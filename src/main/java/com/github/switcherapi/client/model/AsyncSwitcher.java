package com.github.switcherapi.client.model;

import com.github.switcherapi.client.utils.SwitcherUtils;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static final Logger logger = LoggerFactory.getLogger(AsyncSwitcher.class);

	private final ExecutorService executorService;

	private final SwitcherInterface switcherInterface;

	private final long delay;

	private long nextRun = 0;

	public AsyncSwitcher(final SwitcherInterface switcherInterface, long delay) {
		this.executorService = Executors.newCachedThreadPool();
		this.switcherInterface = switcherInterface;
		this.delay = delay;
	}

	/**
	 * Validate if next run is ready to be performed, otherwise it will skip and delegate the
	 * Switcher result for the Switcher history execution.
	 */
	public synchronized void execute() {
		SwitcherUtils.debug(logger, "nextRun: {} - currentTimeMillis: {}", nextRun, System.currentTimeMillis());
		
		if (nextRun < System.currentTimeMillis()) {
			SwitcherUtils.debug(logger, "Running AsyncSwitcher");

			this.nextRun = System.currentTimeMillis() + this.delay;
			this.executorService.submit(this);
		}
	}

	@Override
	public void run() {
		try {
			final CriteriaResponse response = switcherInterface.executeCriteria();

			switcherInterface.getHistoryExecution().removeIf(item ->
					switcherInterface.getSwitcherKey().equals(item.getSwitcherKey()) &&
							switcherInterface.getEntry().equals(item.getEntry()));

			switcherInterface.getHistoryExecution().add(response);
		} catch (SwitcherException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
