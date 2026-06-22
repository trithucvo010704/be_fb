package vn.ezisolutions.cloud.facebook_service.services.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.RedisClient;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.RedisKeys;
import vn.ezisolutions.cloud.facebook_service.core.shared.RoleDef;
import vn.ezisolutions.cloud.facebook_service.entity.id_system.BizUserEntity;

import java.time.Instant;
import java.util.List;

@Service
public class AuthCacheService {

    private static final Logger logger = LoggerFactory.getLogger(AuthCacheService.class);

    @Autowired
    RedisClient redisClient;

    public void cacheAuth(String token, BizUserEntity user, Instant expiredAt) throws JsonProcessingException {
        redisClient.hSet(RedisKeys.TOKEN_KEY, token, user);
        redisClient.hSetExpireAt(RedisKeys.TOKEN_KEY, token, expiredAt);
    }

    public AuthorizedUser getUserByToken(String token) throws CustomException {
        try {
            BizUserEntity userEntity = redisClient.hGet(RedisKeys.TOKEN_KEY, token, BizUserEntity.class);
            if (userEntity == null) {
                throw new CustomException(401, "Unauthenticated");
            }
            return AuthorizedUser.builder()
                    .id(userEntity.getId())
                    .name(userEntity.getName())
                    .username(userEntity.getName())
                    .roles(List.of(RoleDef.USER))
                    .build();
        } catch (JsonProcessingException e) {
            logger.error("JsonProcessingException", e);
            throw new CustomException(401, "Unauthenticated");
        }
    }

    public BizUserEntity getAuth() throws CustomException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof BizUserEntity) {
            return (BizUserEntity) authentication.getPrincipal();
        } else {
            throw new CustomException(401, "Không tìm thấy user");
        }
    }
}
