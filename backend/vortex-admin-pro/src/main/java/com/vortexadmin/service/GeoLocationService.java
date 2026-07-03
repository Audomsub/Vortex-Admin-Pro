package com.vortexadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    // BUG-026: use HTTPS to prevent MITM tampering of geo data; use injected RestTemplate
    private static final String GEO_API = "https://ip-api.com/json/%s?fields=status,country,countryCode";
    private final RestTemplate restTemplate;

    public String[] lookupCountry(String ip) {
        if (ip == null || ip.isBlank() || isPrivateIp(ip)) {
            return new String[]{"Local", "LO"};
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = restTemplate.getForObject(
                    GEO_API.formatted(ip), Map.class);
            if (result != null && "success".equals(result.get("status"))) {
                String country     = (String) result.get("country");
                String countryCode = (String) result.get("countryCode");
                return new String[]{country, countryCode};
            }
        } catch (Exception e) {
            log.warn("Geo lookup failed for IP {}: {}", ip, e.getMessage());
        }
        return new String[]{"Unknown", "XX"};
    }

    private boolean isPrivateIp(String ip) {
        // BUG-027: RFC 1918 172.16.0.0/12 covers 172.16.* through 172.31.*
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                if (second >= 16 && second <= 31) return true;
            } catch (Exception ignored) {}
        }
        return ip.startsWith("127.") || ip.startsWith("192.168.") ||
               ip.startsWith("10.")  ||
               ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }
}
