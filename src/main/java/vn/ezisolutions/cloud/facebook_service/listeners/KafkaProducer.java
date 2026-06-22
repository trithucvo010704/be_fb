package vn.ezisolutions.cloud.facebook_service.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    public void sendEvent(String topic, String key, Object event) throws JsonProcessingException {
        String msgStr = this.mapper.writeValueAsString(event);
        logger.info("[KAFKA] Send key {} to topic {} message: {}", key, topic, msgStr);
        this.kafkaTemplate.send(topic, key, msgStr);
    }
}
