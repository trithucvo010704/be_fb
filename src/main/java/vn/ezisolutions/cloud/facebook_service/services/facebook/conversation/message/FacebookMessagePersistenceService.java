package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbMessageEvent;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class FacebookMessagePersistenceService {

    private final FbPageRepository pageRepository;
    private final FbConversationRepository conversationRepository;
    private final FbMessageRepository messageRepository;

    @Transactional
    public void saveInbound(FbMessageEvent event) {
        if (event == null || event.getEventId() == null || event.getPageId() == null || event.getSenderId() == null) {
            return;
        }
        if (messageRepository.findByEventId(event.getEventId()).isPresent()) {
            return;
        }

        FbPage page = pageRepository.findByFbPageId(event.getPageId())
                .orElse(null);
        if (page == null) {
            return;
        }

        LocalDateTime occurredAt = toLocalDateTime(event.getTimestamp());
        FbConversation conversation = conversationRepository.findByPageIdAndSenderPsid(page.getId(), event.getSenderId())
                .orElseGet(() -> FbConversation.builder()
                        .pageId(page.getId())
                        .senderPsid(event.getSenderId())
                        .status(FbConversation.Status.OPEN)
                        .unreadCount(0)
                        .build());

        conversation.setLastMessageAt(occurredAt);
        conversation.setLastInboundAt(occurredAt);
        conversation.setUnreadCount(conversation.getUnreadCount() == null ? 1 : conversation.getUnreadCount() + 1);
        FbConversation savedConversation = conversationRepository.save(conversation);

        FbMessage.MessageType messageType = toMessageType(event.getType());
        messageRepository.save(FbMessage.builder()
                .conversationId(savedConversation.getId())
                .pageId(page.getId())
                .eventId(event.getEventId())
                .direction(FbMessage.Direction.INBOUND)
                .facebookMessageId(event.getEventId())
                .senderId(event.getSenderId())
                .recipientId(event.getRecipientId())
                .messageType(messageType)
                .content(event.getContent())
                .attachmentUrl(messageType == FbMessage.MessageType.TEXT ? null : event.getContent())
                .status(FbMessage.Status.RECEIVED)
                .occurredAt(occurredAt)
                .build());
    }

    private FbMessage.MessageType toMessageType(MessageType type) {
        if (type == null) {
            return FbMessage.MessageType.UNKNOWN;
        }
        return switch (type) {
            case TEXT -> FbMessage.MessageType.TEXT;
            case IMAGE -> FbMessage.MessageType.IMAGE;
            case FILE, AUDIO, VIDEO -> FbMessage.MessageType.FILE;
            default -> FbMessage.MessageType.UNKNOWN;
        };
    }

    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) {
            return LocalDateTime.now();
        }
        Instant instant = timestamp.toString().length() > 10
                ? Instant.ofEpochMilli(timestamp)
                : Instant.ofEpochSecond(timestamp);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
