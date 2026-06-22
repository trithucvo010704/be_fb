package vn.ezisolutions.cloud.facebook_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FbWebhookObjectType {
    MESSAGING, FEED, UNKNOWN;

    @JsonCreator
    public static FbWebhookObjectType from(String v) {
        if (v == null) return UNKNOWN;
        if ("messaging".equalsIgnoreCase(v)) return MESSAGING;
        if ("feed".equalsIgnoreCase(v)) return FEED;
        try {
            return valueOf(v.toUpperCase());
        } catch (Exception e) {
            return UNKNOWN;
        }
    }

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
