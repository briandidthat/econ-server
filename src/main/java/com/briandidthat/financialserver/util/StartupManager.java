package com.briandidthat.financialserver.util;

import com.briandidthat.financialserver.controller.HealthCheckController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StartupManager {
    private static final Logger logger = LoggerFactory.getLogger(StartupManager.class);
    private static final int expectedTestCount = 2; // Since we have two backends to test (Coinbase, Twelve Data)
    private static final AtomicInteger successfulCount = new AtomicInteger();

    public static synchronized void registerResult(String clazz, boolean status) {
        if (!status) {
            logger.info("{} was unable to connect to the backend.", clazz);
            // if we are unable to reach the backend services, set health to false so kubernetes could restart
            HealthCheckController.setAvailable(false);
            return;
        }
        successfulCount.getAndIncrement();
        logger.info("${} test was successful.", clazz);

        if (successfulCount.get() == expectedTestCount) {
            logger.info("Application is healthy.");
            HealthCheckController.setAvailable(true);
        }
    }
}
