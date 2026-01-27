package com.rcszh.gm.common.security;

/**
 * Use string loginId prefixes to avoid mixing admin users and front-end accounts in Sa-Token.
 * <p>
 * This keeps a single Sa-Token login type but guarantees that:
 * - Admin token cannot be used as an Account token (and vice versa) when endpoints validate the prefix.
 * - Numeric IDs from different tables won't collide.
 */
public final class LoginIdUtils {
    private LoginIdUtils() {
    }

    public static final String ADMIN_PREFIX = "ADMIN_";
    public static final String ACCOUNT_PREFIX = "ACC_";

    public static String adminLoginId(long userId) {
        return ADMIN_PREFIX + userId;
    }

    public static String accountLoginId(long accountId) {
        return ACCOUNT_PREFIX + accountId;
    }

    public static Long parseAdminId(Object loginId) {
        return parseWithPrefix(loginId, ADMIN_PREFIX);
    }

    public static Long parseAccountId(Object loginId) {
        return parseWithPrefix(loginId, ACCOUNT_PREFIX);
    }

    private static Long parseWithPrefix(Object loginId, String prefix) {
        if (loginId == null) {
            return null;
        }
        // Backward compatibility: old tokens might have numeric loginId.
        if (loginId instanceof Long l) {
            return prefix.equals(ADMIN_PREFIX) ? l : null;
        }
        if (loginId instanceof Number n) {
            return prefix.equals(ADMIN_PREFIX) ? n.longValue() : null;
        }
        String s = loginId.toString();
        if (!s.startsWith(prefix)) {
            return null;
        }
        try {
            return Long.parseLong(s.substring(prefix.length()));
        } catch (Exception e) {
            return null;
        }
    }
}

