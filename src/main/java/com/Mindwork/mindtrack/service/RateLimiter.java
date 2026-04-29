package com.Mindwork.mindtrack.service;

public interface RateLimiter {
    /**
     * @return true if request is allowed and counted, false otherwise
     */
    boolean allow(String key, int maxRequests, long windowMillis);
}

