package vn.ezisolutions.cloud.facebook_service.listeners;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.event.KafkaSystemEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbAdsExecutionResponse;

@Service
@RequiredArgsConstructor
public class FbAdsResultProducer {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsResultProducer.class);
    private final KafkaProducer kafkaProducer;

    public void sendExecutionResult(FbAdsExecutionResponse response) throws CustomException {
        if (response == null) return;

        String planId = response.getPlanId();
        KafkaSystemEvent<FbAdsExecutionResponse> event = KafkaSystemEvent.<FbAdsExecutionResponse>builder()
                .name(FbActionEvent.AD_EXECUTION_RESULT.getValue())
                .payload(response)
                .build();

        logger.info("[ADS-PRODUCER] Sending execution response to Kafka topic: {} for PlanID: {}", KafkaTopics.FACEBOOK_AD_RESULT_RESPONSE, planId);
        try {
            kafkaProducer.sendEvent(KafkaTopics.FACEBOOK_AD_RESULT_RESPONSE, planId, event);
        } catch (Exception e) {
            throw new CustomException(500, "Lỗi gửi kết quả thực thi quảng cáo qua Kafka: " + e.getMessage());
        }
        logger.info("[ADS-PRODUCER] Successfully sent message to Kafka for PlanID: {}", planId);
    }
}
