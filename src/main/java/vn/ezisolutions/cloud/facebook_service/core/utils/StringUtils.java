package vn.ezisolutions.cloud.facebook_service.core.utils;

import java.security.SecureRandom;
import java.util.Random;

public class StringUtils {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String DIGITS = "0123456789";

    public static String random(int length) {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(ALPHANUMERIC.length());
            sb.append(ALPHANUMERIC.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static String randomOTP(int length) {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(DIGITS.length());
            sb.append(DIGITS.charAt(randomIndex));
        }

        return sb.toString();
    }

    public static boolean isEmpty(String s) {
        if (s == null) {
            return true;
        }
        return s.isBlank() || s.isEmpty();
    }

    public static String cleanId(String id) {
        return id != null ? id.replace("act_", "").replaceAll("\\D", "") : "";
    }
}
