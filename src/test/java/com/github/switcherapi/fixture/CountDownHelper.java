package com.github.switcherapi.fixture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CountDownHelper {

    private static final Logger logger = LoggerFactory.getLogger(CountDownHelper.class);

    public static void wait(int seconds) {
        try {
            CountDownLatch waiter = new CountDownLatch(1);
            boolean await = waiter.await(seconds, TimeUnit.SECONDS);

            if (!await) {
                waiter.countDown();
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
