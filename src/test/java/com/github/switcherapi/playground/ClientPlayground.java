package com.github.switcherapi.playground;

import static com.github.switcherapi.playground.Features.*;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.model.Switcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientPlayground {

	static final Logger logger = LoggerFactory.getLogger(ClientPlayground.class);

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
	
	public static void test() {
		configure(ContextBuilder.builder()
				.context(Features.class.getCanonicalName())
				.url("https://api.switcherapi.com")
				.apiKey("[API_KEY]")
				.domain("Playground")
                .local(true)
                .snapshotLocation("src/test/resources/snapshot/playground")
				.component("switcher-playground"));
		
		initializeClient();
		Switcher switcher = getSwitcher(MY_SWITCHER);

		scheduler.scheduleAtFixedRate(() -> {
			long time = System.currentTimeMillis();
            logger.info("Switcher is on: {}", switcher.isItOn());
            logger.info("Time elapsed: {}", System.currentTimeMillis() - time);
		}, 0, 5, TimeUnit.SECONDS);
	}
	
	public static void main(String[] args) {
		ClientPlayground.test();
	}
}