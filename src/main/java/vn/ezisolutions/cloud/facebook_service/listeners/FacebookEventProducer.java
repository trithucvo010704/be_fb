package vn.ezisolutions.cloud.facebook_service.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.event.KafkaSystemEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbCommentEvent;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbReactionEvent;

@Service
@RequiredArgsConstructor
public class FacebookEventProducer {
    private static final Logger log = LoggerFactory.getLogger(FacebookEventProducer.class);
    private final KafkaProducer kafkaProducer;

    public void sendCommentEvent(FbCommentEvent event) throws CustomException {
        if (event == null) return;
        String key = event.getData() != null ? event.getData().getFbCommentId() : null;
        send(KafkaTopics.FACEBOOK_WEBHOOK_EVENTS, key, FbActionEvent.WEBHOOK_COMMENT, event);
    }

    public void sendReactionEvent(FbReactionEvent event) throws CustomException {
        if (event == null) return;
        send(KafkaTopics.FACEBOOK_REACTION_WEBHOOK_EVENTS, event.getEventId(), FbActionEvent.WEBHOOK_REACTION, event);
    }

    private void send(String topic, String key, FbActionEvent action, Object payload) throws CustomException {
        try {
            kafkaProducer.sendEvent(topic, key, KafkaSystemEvent.builder().name(action.getValue()).payload(payload).build());
        } catch (Exception e) {
            log.error("Cannot publish Facebook event action={}", action, e);
            throw new CustomException(500, "Cannot publish Facebook event: " + e.getMessage());
        }
    }
}
