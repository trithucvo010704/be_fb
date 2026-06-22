package vn.ezisolutions.cloud.facebook_service.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class RedisClient {

    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    ObjectMapper mapper;

    public @Nullable String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String get(String key, String defaultValue) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? value : defaultValue;
    }

    public @Nullable <T> T getObject(String key, Class<T> objectType) throws JsonProcessingException {
        String value = get(key);
        if (value == null) {
            return null;
        }
        return mapper.readValue(value, objectType);
    }

    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public void set(String key, String value, Instant expiredAt) {
        long seconds = Math.abs(Duration.between(expiredAt, Instant.now()).getSeconds());
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(seconds));
    }

    public <T> void setObject(String key, T object) throws JsonProcessingException {
        redisTemplate.opsForValue().set(key, mapper.writeValueAsString(object));
    }

    public <T> void setObject(String key, T object, long seconds) throws JsonProcessingException {
        redisTemplate.opsForValue().set(key, mapper.writeValueAsString(object), Duration.ofSeconds(seconds));
    }

    public <T> void setObject(String key, T object, Instant expiredAt) throws JsonProcessingException {
        long seconds = Math.abs(Duration.between(expiredAt, Instant.now()).getSeconds());
        redisTemplate.opsForValue().set(key, mapper.writeValueAsString(object), Duration.ofSeconds(seconds));
    }

    public <T> T hGet(String key, String field, Class<T> objectType) throws JsonProcessingException {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String value = hashOps.get(key, field);
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return mapper.readValue(value, objectType);
    }

    public boolean hHas(String key, String field) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        return hashOps.hasKey(key, field);
    }

    public <T> void hSet(String key, String field, T object) throws JsonProcessingException {
        redisTemplate.opsForHash().put(key, field, mapper.writeValueAsString(object));
    }

    public <T> void hSetExpireAt(String key, String field, Instant time) {
        redisTemplate.opsForHash().expireAt(key, time, List.of(field));
    }

    public <T> void hDel(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }
}
