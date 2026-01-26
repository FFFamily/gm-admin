package com.rcszh.gm.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class RequestUtils {
    private RequestUtils() {
    }

    public static HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }

    public static String requestId() {
        var req = currentRequest();
        if (req == null) {
            return null;
        }
        Object v = req.getAttribute(RequestIdFilter.ATTR_REQUEST_ID);
        return v == null ? null : v.toString();
    }

    public static String userAgent() {
        var req = currentRequest();
        if (req == null) {
            return null;
        }
        return req.getHeader("User-Agent");
    }

    public static String clientIp() {
        var req = currentRequest();
        if (req == null) {
            return null;
        }
        // Minimal IP extraction (trusted proxy handling can be added later).
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        String xrip = req.getHeader("X-Real-IP");
        if (xrip != null && !xrip.isBlank()) {
            return xrip.trim();
        }
        return req.getRemoteAddr();
    }
}

