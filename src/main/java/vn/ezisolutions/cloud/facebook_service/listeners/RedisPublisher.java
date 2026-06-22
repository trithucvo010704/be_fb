package vn.ezisolutions.cloud.facebook_service.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPublisher {

    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    ObjectMapper mapper;

    public void publish(String channel, String message) {
        redisTemplate.convertAndSend(channel, message);
    }

    public void publishObject(String channel, Object message) throws JsonProcessingException {
        redisTemplate.convertAndSend(channel, mapper.writeValueAsString(message));
    }
}
