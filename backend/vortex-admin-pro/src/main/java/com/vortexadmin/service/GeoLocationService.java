package com.vortexadmin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Service responsible for resolving IP addresses to their corresponding country using the
 * ip-api.com REST API.  Private and loopback IP ranges are resolved locally without making
 * an external HTTP call.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeoLocationService {

    private static final String GEO_API = "https://ip-api.com/json/%s?fields=status,country,countryCode";
    private final RestTemplate restTemplate;

    /**
     * Resolves the given IP address to a country name and ISO 3166-1 alpha-2 country code.
     * <ul>
     *   <li>Null, blank, or private/loopback addresses return {@code ["Local", "LO"]}.</li>
     *   <li>Successful lookups return {@code [countryName, countryCode]}.</li>
     *   <li>Failed or unsuccessful API responses return {@code ["Unknown", "XX"]}.</li>
     * </ul>
     *
     * @param ip the IPv4 or IPv6 address to look up
     * @return a two-element array where index 0 is the country name and index 1 is the
     *         ISO country code; never {@code null}
     */
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

    /**
     * Determines whether the given IP address belongs to a private or loopback range that
     * should not be sent to an external geolocation API.
     * <p>
     * Recognised private ranges:
     * <ul>
     *   <li>Loopback: {@code 127.0.0.0/8}</li>
     *   <li>Class A private: {@code 10.0.0.0/8}</li>
     *   <li>Class B private: {@code 172.16.0.0/12} (172.16.* through 172.31.*)</li>
     *   <li>Class C private: {@code 192.168.0.0/16}</li>
     *   <li>IPv6 loopback: {@code ::1}</li>
     * </ul>
     *
     * @param ip the IP address string to evaluate
     * @return {@code true} if the address is within a private or loopback range,
     *         otherwise {@code false}
     */
    private boolean isPrivateIp(String ip) {
        // RFC 1918 172.16.0.0/12 covers 172.16.* through 172.31.* (not just 172.16.*)
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
