package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FbMessageResponse(
        @JsonProperty("recipient_id")
        String recipientId,

        @JsonProperty("message_id")
        String messageId
) {}
