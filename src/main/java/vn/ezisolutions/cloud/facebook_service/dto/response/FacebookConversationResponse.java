package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;

import java.time.LocalDateTime;
import java.util.UUID;

public record FacebookConversationResponse(
        UUID id,
        UUID pageId,
        String senderPsid,
        String senderName,
        String senderAvatarUrl,
        FbConversation.Status status,
        LocalDateTime lastMessageAt,
        LocalDateTime lastInboundAt,
        LocalDateTime lastOutboundAt,
        Integer unreadCount
) {
    public static FacebookConversationResponse from(FbConversation conversation) {
        return new FacebookConversationResponse(
                conversation.getId(),
                conversation.getPageId(),
                conversation.getSenderPsid(),
                conversation.getSenderName(),
                conversation.getSenderAvatarUrl(),
                conversation.getStatus(),
                conversation.getLastMessageAt(),
                conversation.getLastInboundAt(),
                conversation.getLastOutboundAt(),
                conversation.getUnreadCount()
        );
    }
}

