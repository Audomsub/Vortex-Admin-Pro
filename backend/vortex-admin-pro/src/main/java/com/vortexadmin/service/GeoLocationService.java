package com.vortexadmin.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class GeoLocationService {

    private static final String GEO_API = "http://ip-api.com/json/%s?fields=status,country,countryCode";
    private final RestTemplate restTemplate = new RestTemplate();

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
        return ip.startsWith("127.") || ip.startsWith("192.168.") ||
               ip.startsWith("10.")  || ip.startsWith("172.16.") ||
               ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1");
    }
}
