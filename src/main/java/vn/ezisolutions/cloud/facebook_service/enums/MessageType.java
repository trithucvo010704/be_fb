package vn.ezisolutions.cloud.facebook_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, UNKNOWN;

    @JsonCreator
    public static MessageType from(String v) {
        if (v == null) return UNKNOWN;
        try { return valueOf(v.toUpperCase()); } catch (Exception e) { return UNKNOWN; }
    }

    @JsonValue
    public String toValue() { return name().toLowerCase(); }

    public boolean isAttachment() {
        return this == IMAGE || this == VIDEO || this == AUDIO || this == FILE;
    }
}
