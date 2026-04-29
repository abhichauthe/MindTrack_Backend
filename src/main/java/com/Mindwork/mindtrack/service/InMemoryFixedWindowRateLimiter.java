package com.Mindwork.mindtrack.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryFixedWindowRateLimiter implements RateLimiter {

    private static final class Window {
        long windowStartMillis;
        int count;
    }

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public boolean allow(String key, int maxRequests, long windowMillis) {
        long now = System.currentTimeMillis();
        Window w = windows.compute(key, (k, existing) -> {
            if (existing == null) {
                Window nw = new Window();
                nw.windowStartMillis = now;
                nw.count = 1;
                return nw;
            }
            if (now - existing.windowStartMillis >= windowMillis) {
                existing.windowStartMillis = now;
                existing.count = 1;
                return existing;
            }
            if (existing.count >= maxRequests) {
                return existing;
            }
            existing.count += 1;
            return existing;
        });
        return w.count <= maxRequests && (now - w.windowStartMillis) < windowMillis;
    }
}

