package com.vortexadmin.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory rate-limiting service for API key access, enforcing configurable per-minute
 * and per-hour request quotas using sliding-window counters stored in a thread-safe map.
 */
@Service
public class ApiRateLimitService {

    // keyHash -> [minuteCount, minuteWindowStart, hourCount, hourWindowStart]
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

    /**
     * Checks whether the given API key is within its configured rate limits and increments
     * the associated request counters.  The counters are automatically reset when the current
     * minute or hour window has elapsed.
     *
     * @param keyHash        the hashed API key string used as the map key
     * @param limitPerMinute the maximum number of requests allowed per minute;
     *                       {@code null} or {@code <= 0} means no per-minute limit
     * @param limitPerHour   the maximum number of requests allowed per hour;
     *                       {@code null} or {@code <= 0} means no per-hour limit
     * @return {@code true} if the request is within the configured limits (allowed),
     *         {@code false} if either the per-minute or per-hour quota has been exceeded
     */
    public boolean isAllowed(String keyHash, Integer limitPerMinute, Integer limitPerHour) {
        long now = System.currentTimeMillis();

        long[] state = counters.compute(keyHash, (k, v) -> {
            if (v == null) v = new long[]{0, now, 0, now};
            if (now - v[1] >= 60_000L)      { v[0] = 0; v[1] = now; }
            if (now - v[3] >= 3_600_000L)   { v[2] = 0; v[3] = now; }
            v[0]++;
            v[2]++;
            return v;
        });

        boolean minuteOk = (limitPerMinute == null || limitPerMinute <= 0 || state[0] <= limitPerMinute);
        boolean hourOk   = (limitPerHour   == null || limitPerHour   <= 0 || state[2] <= limitPerHour);
        return minuteOk && hourOk;
    }

    /**
     * Returns the current rate-limit counters for the specified API key without modifying them.
     * The returned array contains four elements:
     * <ol>
     *   <li>current-minute request count</li>
     *   <li>current-minute window start (epoch millis)</li>
     *   <li>current-hour request count</li>
     *   <li>current-hour window start (epoch millis)</li>
     * </ol>
     *
     * @param keyHash the hashed API key string to look up
     * @return the counter array for the key, or {@code [0, 0, 0, 0]} if no counters exist yet
     */
    public long[] getCounters(String keyHash) {
        return counters.getOrDefault(keyHash, new long[]{0, 0, 0, 0});
    }
}
