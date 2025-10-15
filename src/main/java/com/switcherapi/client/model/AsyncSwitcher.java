package com.switcherapi.client.model;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.utils.SwitcherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.switcherapi.client.service.WorkerName.SWITCHER_ASYNC_WORKER;

/**
 * Implementation handle asynchronous Criteria execution when using throttle.
 * <br>Threads are only created when the time calculated for the next run is lower than the current time.
 * 
 * @author Roger Floriano (petruki)
 * @since 2021-11-27
 *
 */
public class AsyncSwitcher {

	private static final Logger logger = LoggerFactory.getLogger(AsyncSwitcher.class);

	private final ExecutorService executorService;

	private final Switcher switcher;

	private final long delay;

	private long nextRun = 0;

	public AsyncSwitcher(final Switcher switcher, long delay) {
		this.executorService = Executors.newCachedThreadPool(r -> {
			Thread thread = new Thread(r);
			thread.setName(SWITCHER_ASYNC_WORKER.toString());
			thread.setDaemon(true);
			return thread;
		});

		this.switcher = switcher;
		this.delay = delay;
	}

	/**
	 * Validate if next run is ready to be performed, otherwise it will skip and delegate the
	 * Switcher result for the Switcher history execution.
	 */
	public void execute() {
		SwitcherUtils.debug(logger, "nextRun: {} - currentTimeMillis: {}", nextRun, System.currentTimeMillis());
		
		if (nextRun < System.currentTimeMillis()) {
			SwitcherUtils.debug(logger, "Running AsyncSwitcher");

			this.nextRun = System.currentTimeMillis() + this.delay;
			this.executorService.submit(this::run);
		}
	}

	public void run() {
		try {
			final SwitcherResult response = switcher.executeCriteria();
			switcher.updateHistoryExecution(response);
		} catch (SwitcherException e) {
			logger.error(e.getMessage(), e);
		}
	}

}
