package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;

import java.time.LocalDateTime;
import java.util.UUID;

public record FacebookMessageResponse(
        UUID id,
        UUID conversationId,
        UUID pageId,
        FbMessage.Direction direction,
        String facebookMessageId,
        String senderId,
        String recipientId,
        FbMessage.MessageType messageType,
        String content,
        String attachmentUrl,
        FbMessage.Status status,
        String errorMessage,
        LocalDateTime occurredAt
) {
    public static FacebookMessageResponse from(FbMessage message) {
        return new FacebookMessageResponse(
                message.getId(),
                message.getConversationId(),
                message.getPageId(),
                message.getDirection(),
                message.getFacebookMessageId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getMessageType(),
                message.getContent(),
                message.getAttachmentUrl(),
                message.getStatus(),
                message.getErrorMessage(),
                message.getOccurredAt()
        );
    }
}

