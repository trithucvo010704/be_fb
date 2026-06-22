package vn.ezisolutions.cloud.facebook_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FbFeedItemType {
    COMMENT, REACTION, UNKNOWN;

    @JsonCreator
    public static FbFeedItemType from(String v) {
        if (v == null) return UNKNOWN;
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
