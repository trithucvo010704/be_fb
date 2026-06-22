package vn.ezisolutions.cloud.facebook_service.services.facebook.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbMessageEvent;
import vn.ezisolutions.cloud.facebook_service.dto.webhook.FbWebhookPayload;
import vn.ezisolutions.cloud.facebook_service.enums.FbWebhookObjectType;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;
import vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message.FacebookMessagePersistenceService;

@Component
@RequiredArgsConstructor
public class FbMessagingWebhookHandler implements IFbWebhookHandler {

    private final FacebookMessagePersistenceService messagePersistenceService;

    @Override
    public boolean supports(FbWebhookObjectType objectType, Object payload) {
        return objectType == FbWebhookObjectType.MESSAGING && payload instanceof FbWebhookPayload.FbWebhookMessaging;
    }

    @Override
    public void handle(String pageId, Object payload, String eventId) throws CustomException {
        try {
            FbWebhookPayload.FbWebhookMessaging msg = (FbWebhookPayload.FbWebhookMessaging) payload;
            FbWebhookPayload.FbWebhookMessage messageObj = msg.getMessage();
            if (messageObj == null || Boolean.TRUE.equals(messageObj.getIsEcho())) {
                return;
            }

            String senderId = msg.getSender() != null ? msg.getSender().getId() : "";
            String content = "";
            MessageType type = MessageType.TEXT;

            if (messageObj.getText() != null) {
                content = messageObj.getText();
            } else if (messageObj.getAttachments() != null && !messageObj.getAttachments().isEmpty()) {
                FbWebhookPayload.FbWebhookAttachment attachment = messageObj.getAttachments().get(0);
                if (attachment.getPayload() != null) {
                    content = attachment.getPayload().getUrl();
                }
                type = MessageType.from(attachment.getType());
            }

            FbMessageEvent event = FbMessageEvent.builder()
                    .eventId(eventId)
                    .pageId(pageId)
                    .senderId(senderId)
                    .recipientId(msg.getRecipient() != null ? msg.getRecipient().getId() : pageId)
                    .content(content)
                    .type(type)
                    .timestamp(msg.getTimestamp() != null ? msg.getTimestamp() : System.currentTimeMillis())
                    .build();

            messagePersistenceService.saveInbound(event);
        } catch (Exception e) {
            throw new CustomException(500, "Error handling messaging webhook: " + e.getMessage());
        }
    }
}
