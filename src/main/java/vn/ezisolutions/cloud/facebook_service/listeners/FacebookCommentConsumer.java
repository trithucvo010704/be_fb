package vn.ezisolutions.cloud.facebook_service.listeners;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.event.KafkaSystemEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbCommentEvent;
import vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.comment.FacebookCommentActionService;

@Service
@ConditionalOnProperty(prefix = "facebook.features.comments", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FacebookCommentConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FacebookCommentConsumer.class);
    private static final String TOPIC = KafkaTopics.FACEBOOK_COMMENT_RESPONSE;

    private final ObjectMapper objectMapper;
    private final FacebookCommentActionService facebookCommentActionService;

    @KafkaListener(topics = TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<String, String> consumerRecord) {
        try {
            if (consumerRecord.value() == null || consumerRecord.value().trim().isEmpty()) {
                logger.warn("[COMMENT-CONSUMER] Skip empty comment event");
                return;
            }

            KafkaSystemEvent<FbCommentEvent.CommentData> event = objectMapper.readValue(
                    consumerRecord.value(), new TypeReference<KafkaSystemEvent<FbCommentEvent.CommentData>>() {
                    });

            if (event == null || event.getName() == null || event.getPayload() == null) {
                logger.warn("[COMMENT-CONSUMER] Skip invalid comment event: missing name or payload");
                return;
            }

            FbActionEvent actionEvent = FbActionEvent.fromValue(event.getName());
            FbCommentEvent.CommentData data = event.getPayload();
            logger.info("[COMMENT-CONSUMER] Received action: [{}] for commentId: {}",
                    actionEvent.getValue(), data.getFbCommentId());

            facebookCommentActionService.processAction(actionEvent, data);

        } catch (IllegalArgumentException e) {
            logger.warn("[COMMENT-CONSUMER] Skip comment event: unknown/unsupported action. Committing offset.");
        } catch (CustomException e) {
            if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                logger.warn("[COMMENT-CONSUMER] Business error [{}]: {}. Committing offset.", e.getStatusCode(),
                        e.getMessage());
            } else {
                logger.error("[COMMENT-CONSUMER] System business error [{}]: {}. Rethrowing.", e.getStatusCode(),
                        e.getMessage());
                throw new IllegalStateException("Lỗi nghiệp vụ hệ thống xử lý phản hồi comment", e);
            }
        } catch (Exception e) {
            logger.error("[COMMENT-CONSUMER] System error processing message. Rethrowing.", e);
            throw new IllegalStateException("Lỗi hệ thống xử lý phản hồi comment", e);
        }
    }
}
