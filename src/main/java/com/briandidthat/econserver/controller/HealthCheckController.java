package com.briandidthat.econserver.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
public class HealthCheckController {
    private static final Logger logger = LoggerFactory.getLogger("HealthCheckController");
    private static final AtomicBoolean available = new AtomicBoolean(false);
    private final String AVAILABLE = "AVAILABLE";
    private final String UNAVAILABLE = "UNAVAILABLE";

    public static void setAvailable(boolean status) {
        if (!status) logger.error("Application unhealthy. Setting unavailable...");
        available.set(status);
    }

    @GetMapping("/healthz")
    public ResponseEntity<String> isAvailable() {
        logger.info("Application Health: {}", available.get() ? AVAILABLE : UNAVAILABLE);
        if (!available.get()) return ResponseEntity.internalServerError().body(UNAVAILABLE);
        return ResponseEntity.ok(AVAILABLE);
    }

    @GetMapping("/readyz")
    public ResponseEntity<String> isReady() {
        if (!available.get()) return ResponseEntity.internalServerError().body(UNAVAILABLE);
        logger.info("Application Health: {}", available.get() ? AVAILABLE : UNAVAILABLE);
        return ResponseEntity.ok(AVAILABLE);
    }
}
