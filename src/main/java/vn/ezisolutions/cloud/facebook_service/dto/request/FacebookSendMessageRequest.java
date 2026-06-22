package vn.ezisolutions.cloud.facebook_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;

public record FacebookSendMessageRequest(
        @NotBlank(message = "recipientPsid không được để trống")
        String recipientPsid,

        @NotBlank(message = "content không được để trống")
        String content,

        FbMessage.MessageType messageType
) {
    public FbMessage.MessageType resolvedMessageType() {
        return messageType == null ? FbMessage.MessageType.TEXT : messageType;
    }
}

