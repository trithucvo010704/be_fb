package vn.ezisolutions.cloud.facebook_service.services.facebook.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.AbstractCacheService;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.RedisClient;

@Service
public class IdSystemCacheService extends AbstractCacheService {

    public IdSystemCacheService(RedisClient client) {
        super(client, "id_system");
    }

    public AuthorizedUser getUser(String token) throws JsonProcessingException {
        return get("tokens", token, AuthorizedUser.class);
    }
}
