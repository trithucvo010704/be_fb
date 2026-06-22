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
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbPostingPlanEvent;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.FacebookPostService;

@Service
@ConditionalOnProperty(prefix = "facebook.features.scheduled-posts", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FacebookPostConsumer {

    private static final Logger logger = LoggerFactory.getLogger(FacebookPostConsumer.class);

    private final FacebookPostService facebookPostService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.FACEBOOK_POSTING_PLAN, groupId = "${spring.application.name}")
    public void consumePostingPlan(ConsumerRecord<String, String> consumerRecord) {
        try {
            if (consumerRecord.value() == null || consumerRecord.value().trim().isEmpty()) {
                logger.warn("[POST-CONSUMER] Skip empty posting plan event");
                return;
            }

            KafkaSystemEvent<FbPostingPlanEvent> event = objectMapper.readValue(
                    consumerRecord.value(), new TypeReference<KafkaSystemEvent<FbPostingPlanEvent>>() {
                        // Empty on purpose for Jackson TypeReference
                    });

            if (event == null || event.getName() == null || event.getPayload() == null) {
                logger.warn("[POST-CONSUMER] Skip invalid posting plan event: missing name or payload");
                return;
            }

            FbActionEvent actionEvent;
            try {
                actionEvent = FbActionEvent.fromValue(event.getName());
            } catch (IllegalArgumentException e) {
                logger.warn("[POST-CONSUMER] Skip posting plan event: unknown action={}", event.getName());
                return;
            }

            if (actionEvent == FbActionEvent.POSTING_PLAN) {
                FbPostingPlanEvent payload = event.getPayload();
                if (payload.getOwnerId() != null) {
                    facebookPostService.savePostPlan(payload);
                    logger.info("[POST-CONSUMER] Successfully saved posting plan - user: {}",
                            payload.getOwnerId());
                } else {
                    logger.warn("[POST-CONSUMER] Skip posting plan event: ownerId is null");
                }
            } else {
                logger.warn("[POST-CONSUMER] Skip posting plan event: unsupported action={}", actionEvent.getValue());
            }
        } catch (Exception e) {
            logger.error("[POST-CONSUMER] Error processing posting plan event - error: {}. Rethrowing.", e.getMessage(),
                    e);
            throw new IllegalStateException("Lỗi hệ thống xử lý kế hoạch đăng bài", e);
        }
    }
}
