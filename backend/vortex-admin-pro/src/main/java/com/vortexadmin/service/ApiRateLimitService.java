package com.vortexadmin.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class ApiRateLimitService {

    // keyHash -> [minuteCount, minuteWindowStart, hourCount, hourWindowStart]
    private final ConcurrentHashMap<String, long[]> counters = new ConcurrentHashMap<>();

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

    public long[] getCounters(String keyHash) {
        return counters.getOrDefault(keyHash, new long[]{0, 0, 0, 0});
    }
}
