package com.github.switcherapi.playground;

import static com.github.switcherapi.playground.Features.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.switcherapi.client.ContextBuilder;
import com.github.switcherapi.client.model.Switcher;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientPlayground {
	
	final static Logger logger = LogManager.getLogger(ClientPlayground.class);

	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public static void test() {
		configure(ContextBuilder.builder()
				.contextLocation(Features.class.getCanonicalName())
				.url("https://api.switcherapi.com")
				.apiKey("JDJiJDA4JEFweTZjSTR2bE9pUjNJOUYvRy9raC4vRS80Q2tzUnk1d3o1aXFmS2o5eWJmVW11cjR0ODNT")
				.domain("Playground")
				.component("switcher-playground"));

		initializeClient();
		Switcher switcher = getSwitcher(MY_SWITCHER);
		switcher.setShowReason(true);

		scheduler.scheduleAtFixedRate(() -> {
			long time = System.currentTimeMillis();
            logger.info("Switcher is on: {}", switcher.throttle(1000).isItOn());
            logger.info("Time elapsed: {}", System.currentTimeMillis() - time);
		}, 0, 2, TimeUnit.SECONDS);
	}

	public static void main(String[] args) {
		ClientPlayground.test();
	}
}