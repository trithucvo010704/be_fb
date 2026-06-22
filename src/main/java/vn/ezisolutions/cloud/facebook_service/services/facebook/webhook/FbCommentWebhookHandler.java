package vn.ezisolutions.cloud.facebook_service.services.facebook.webhook;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbCommentEvent;
import vn.ezisolutions.cloud.facebook_service.dto.webhook.FbWebhookPayload;
import vn.ezisolutions.cloud.facebook_service.enums.FbFeedItemType;
import vn.ezisolutions.cloud.facebook_service.enums.FbWebhookObjectType;
import vn.ezisolutions.cloud.facebook_service.listeners.FacebookEventProducer;

@Component
@ConditionalOnProperty(prefix = "facebook.features.comments", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FbCommentWebhookHandler implements IFbWebhookHandler {

    private final FacebookEventProducer facebookEventProducer;

    @Override
    public boolean supports(FbWebhookObjectType objectType, Object payload) {
        if (objectType != FbWebhookObjectType.FEED || !(payload instanceof FbWebhookPayload.FbWebhookChange change)) {
            return false;
        }
        if (change.getValue() == null) {
            return false;
        }
        return FbFeedItemType.from(change.getValue().getItem()) == FbFeedItemType.COMMENT;
    }

    @Override
    public void handle(String pageId, Object payload, String eventId) throws CustomException {
        try {
            FbWebhookPayload.FbWebhookChange change = (FbWebhookPayload.FbWebhookChange) payload;
            FbWebhookPayload.FbWebhookValue value = change.getValue();
            
            String verb = value.getVerb();
            if (!FacebookConstants.Webhook.VERB_ADD.equals(verb) && !FacebookConstants.Webhook.VERB_EDIT.equals(verb)) {
                return;
            }

            FbWebhookPayload.FbWebhookFrom from = value.getFrom();
            FbCommentEvent.Sender sender = FbCommentEvent.Sender.builder()
                    .id(from != null ? from.getId() : "")
                    .name(from != null ? from.getName() : "")
                    .build();

            FbCommentEvent.CommentData commentData = FbCommentEvent.CommentData.builder()
                    .fbPageId(pageId)
                    .fbPostId(value.getPostId())
                    .fbCommentId(value.getCommentId())
                    .parentId(value.getParentId())
                    .message(value.getMessage())
                    .createdTime(value.getCreatedTime() != null ? value.getCreatedTime() : 0L)
                    .sender(sender)
                    .build();

            FbCommentEvent commentEvent = FbCommentEvent.builder()
                    .eventType(verb)
                    .timestamp(System.currentTimeMillis())
                    .data(commentData)
                    .build();

            facebookEventProducer.sendCommentEvent(commentEvent);
        } catch (Exception e) {
            throw new CustomException(500, "Error handling comment webhook: " + e.getMessage());
        }
    }
}
