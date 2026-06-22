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
import vn.ezisolutions.cloud.facebook_service.dto.event.FbSyncJobEvent;
import vn.ezisolutions.cloud.facebook_service.services.facebook.auth.FacebookPermissionService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.sync.FacebookSyncInsightService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.sync.FacebookSyncService;

@Service
@ConditionalOnProperty(prefix = "facebook.features.sync-history", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FacebookSyncConsumer {
    private static final Logger logger = LoggerFactory.getLogger(FacebookSyncConsumer.class);
    private final ObjectMapper objectMapper;
    private final FacebookSyncService facebookSyncService;
    private final FacebookSyncInsightService facebookSyncInsightService;
    private final FacebookPermissionService facebookPermissionService;

    @KafkaListener(topics = KafkaTopics.FACEBOOK_SYNC_REQUEST, groupId = "${spring.application.name}")
    public void consumeSync(ConsumerRecord<String, String> consumerRecord) {
        try {
            if (consumerRecord.value() == null || consumerRecord.value().trim().isEmpty()) {
                logger.warn("[SYNC-CONSUMER] Skip empty sync event");
                return;
            }

            KafkaSystemEvent<FbSyncJobEvent> event = objectMapper.readValue(
                    consumerRecord.value(), new TypeReference<KafkaSystemEvent<FbSyncJobEvent>>() {
                        // Empty on purpose for Jackson TypeReference
                    });

            if (event == null || event.getName() == null || event.getPayload() == null) {
                logger.warn("[SYNC-CONSUMER] Skip invalid sync event: missing name or payload");
                return;
            }

            FbActionEvent actionEvent;
            try {
                actionEvent = FbActionEvent.fromValue(event.getName());
            } catch (IllegalArgumentException e) {
                logger.warn("[SYNC-CONSUMER] Skip sync event: unknown action={}", event.getName());
                return;
            }

            FbSyncJobEvent payload = event.getPayload();

            if (actionEvent == FbActionEvent.SYNC_PAGE_HISTORY) {
                logger.info("[SYNC-CONSUMER] Consumer nhận job đồng bộ LỊCH SỬ cho Page: {}", payload.getPageId());
                facebookSyncService.processSyncHistory(payload.getPageId());
            } else if (actionEvent == FbActionEvent.SYNC_PAGE_INSIGHTS) {
                logger.info("[SYNC-CONSUMER] Consumer nhận job đồng bộ INSIGHT cho Page: {}", payload.getPageId());
                facebookSyncInsightService.processSyncInsights(payload.getPageId(), payload.getPostIds());
            } else if (actionEvent == FbActionEvent.PAGE_CONNECTED) {
                logger.info("[SYNC-CONSUMER] Consumer nhận job đồng bộ sau đăng nhập - Owner: {}, User: {}", payload.getOwnerId(), payload.getPageId());
                facebookPermissionService.processSyncAfterLogin(payload.getOwnerId(), payload.getPageId());
            } else {
                logger.warn("[SYNC-CONSUMER] Skip sync event: unsupported action={}", actionEvent.getValue());
            }
        } catch (Exception e) {
            logger.error("[SYNC-CONSUMER] Lỗi khi xử lý Kafka message (Sync Job) - error: {}. Rethrowing.",
                    e.getMessage(), e);
            throw new IllegalStateException("Lỗi hệ thống đồng bộ dữ liệu Facebook", e);
        }
    }
}
