package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.shared.RedisKeys;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;
import vn.ezisolutions.cloud.facebook_service.services.security.TokenCryptoService;

@Service
@RequiredArgsConstructor
public class FacebookTokenService {
    private static final Logger log = LoggerFactory.getLogger(FacebookTokenService.class);
    private final FbPageRepository fbPageRepository;
    private final FbUserRepository fbUserRepository;
    private final FacebookCacheService cacheService;
    private final TokenCryptoService tokenCryptoService;
    private static final String EMPTY_TOKEN_MARKER = RedisKeys.EMPTY_TOKEN_MARKER;

    private final Object pageTokenLock = new Object();
    private final Object userTokenLock = new Object();

    public String getPageAccessToken(String pageId) {
        if (pageId == null || pageId.isBlank()) {
            return null;
        }
        String cached = getCachedPageToken(pageId);
        if (cached != null) {
            return EMPTY_TOKEN_MARKER.equals(cached) ? null : cached;
        }

        synchronized (pageTokenLock) {
            // Double-check trong lock
            cached = getCachedPageToken(pageId);
            if (cached != null) {
                return EMPTY_TOKEN_MARKER.equals(cached) ? null : cached;
            }

            log.info(">>> Fetching Page Token từ Database cho Page: {}", pageId);
            String token = fbPageRepository.findByFbPageId(pageId)
                    .filter(page -> page.getTokenStatus() == FbPage.TokenStatus.ACTIVE)
                    .map(FbPage::getPageAccessToken)
                    .map(tokenCryptoService::decrypt)
                    .orElse(null);

            cachePageTokenSafely(pageId, token);
            return token;
        }
    }

    public String getUserAccessToken(String fbUserId) {
        if (fbUserId == null || fbUserId.isBlank()) {
            return null;
        }
        String cached = getCachedUserToken(fbUserId);
        if (cached != null) {
            return EMPTY_TOKEN_MARKER.equals(cached) ? null : cached;
        }

        synchronized (userTokenLock) {
            cached = getCachedUserToken(fbUserId);
            if (cached != null) {
                return EMPTY_TOKEN_MARKER.equals(cached) ? null : cached;
            }

            log.info("Fetching User Token từ Database cho User: {}", fbUserId);
            String token = fbUserRepository.findByFbUserId(fbUserId)
                    .map(FbUser::getAccessToken)
                    .map(tokenCryptoService::decrypt)
                    .orElse(null);

            cacheUserTokenSafely(fbUserId, token);
            return token;
        }
    }

    public void evictPageToken(String pageId) {
        try {
            cacheService.evictPageToken(pageId);
        } catch (Exception e) {
            log.error("REDIS ERROR (evict): {}", e.getMessage());
        }
    }

    private String getCachedPageToken(String pageId) {
        try {
            return cacheService.getPageToken(pageId);
        } catch (Exception e) {
            log.warn("REDIS ERROR (getPageToken) cho Page {}: {}. Fallback dùng DB.", pageId, e.getMessage());
            return null;
        }
    }

    private String getCachedUserToken(String fbUserId) {
        try {
            return cacheService.getUserToken(fbUserId);
        } catch (Exception e) {
            log.warn("REDIS ERROR (getUserToken) cho User {}: {}. Fallback dùng DB.", fbUserId, e.getMessage());
            return null;
        }
    }

    private void cachePageTokenSafely(String pageId, String token) {
        try {
            if (token != null) {
                cacheService.cachePageToken(pageId, token);
            } else {
                cacheService.cacheString(RedisKeys.NS_PAGE_TOKENS, pageId, EMPTY_TOKEN_MARKER, 60);
            }
        } catch (Exception e) {
            log.warn("REDIS ERROR: Không thể cache Page Token cho {}: {}", pageId, e.getMessage());
        }
    }

    private void cacheUserTokenSafely(String fbUserId, String token) {
        try {
            if (token != null) {
                cacheService.cacheUserToken(fbUserId, token);
            } else {
                cacheService.cacheString(RedisKeys.NS_USER_TOKENS, fbUserId, EMPTY_TOKEN_MARKER, 60);
            }
        } catch (Exception e) {
            log.warn("REDIS ERROR: Không thể cache User Token cho {}: {}", fbUserId, e.getMessage());
        }
    }
}
