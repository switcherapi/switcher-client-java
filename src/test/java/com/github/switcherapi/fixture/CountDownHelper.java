package com.github.switcherapi.fixture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownHelper {

    private static final Logger logger = LogManager.getLogger(CountDownHelper.class);

    public static void wait(int seconds) {
        try {
            CountDownLatch waiter = new CountDownLatch(1);
            boolean finished = waiter.await(seconds, TimeUnit.SECONDS);

            if (!finished) {
                logger.error("Countdown failed");
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
