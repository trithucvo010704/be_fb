package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.RedisClient;
import vn.ezisolutions.cloud.facebook_service.core.shared.RedisKeys;

@Service
@RequiredArgsConstructor
public class FacebookCacheService {

    @Value("${cache.ttl.user-token:900}")
    private long userTokenTtl;

    @Value("${cache.ttl.page-token:1800}")
    private long pageTokenTtl;

    private static final Logger log = LoggerFactory.getLogger(FacebookCacheService.class);
    private final RedisClient client;
    private final RedisTemplate<String, String> redisTemplate;

    private String buildKey(String namespace, String id) {
        return String.format("%s:%s:%s", RedisKeys.FB_PREFIX, namespace, id);
    }

    public void cacheString(String namespace, String id, String value, long ttl) {
        client.set(buildKey(namespace, id), value, ttl);
    }

    public String getString(String namespace, String id) {
        return client.get(buildKey(namespace, id));
    }

    public void evict(String namespace, String id) {
        redisTemplate.delete(buildKey(namespace, id));
    }

    public void cacheUserToken(String fbUserId, String token) {
        try {
            cacheString(RedisKeys.NS_USER_TOKENS, fbUserId, token, userTokenTtl);
        } catch (Exception e) {
            log.warn("[REDIS-ERROR] Lỗi khi lưu User Token ({}). Bỏ qua thao tác cache.", fbUserId, e);
        }
    }

    public String getUserToken(String fbUserId) {
        try {
            return getString(RedisKeys.NS_USER_TOKENS, fbUserId);
        } catch (Exception e) {
            log.warn("[REDIS-ERROR] Lỗi kết nối Redis khi đọc User Token ({}). Coi như Cache Miss.", fbUserId, e);
            return null;
        }
    }

    public void cachePageToken(String pageId, String token) {
        try {
            cacheString(RedisKeys.NS_PAGE_TOKENS, pageId, token, pageTokenTtl);
        } catch (Exception e) {
            log.warn("[REDIS-ERROR] Lỗi khi lưu Page Token ({}). Bỏ qua thao tác cache.", pageId, e);
        }
    }

    public String getPageToken(String pageId) {
        try {
            return getString(RedisKeys.NS_PAGE_TOKENS, pageId);
        } catch (Exception e) {
            log.warn("[REDIS-ERROR] Lỗi kết nối Redis khi đọc Page Token ({}). Coi như Cache Miss.", pageId, e);
            return null;
        }
    }

    public void evictPageToken(String pageId) {
        try {
            evict(RedisKeys.NS_PAGE_TOKENS, pageId);
        } catch (Exception e) {
            log.warn("[REDIS-ERROR] Lỗi khi xóa Page Token cache ({}).", pageId, e);
        }
    }
}
