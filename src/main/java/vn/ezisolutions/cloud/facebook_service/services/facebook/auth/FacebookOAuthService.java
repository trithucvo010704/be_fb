package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthUrlResponse;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookOAuthService {
    private final FacebookProperties properties;

    public FacebookOAuthUrlResponse buildAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        String url = UriComponentsBuilder
                .fromUriString("https://www.facebook.com/" + properties.getGraphVersion() + "/dialog/oauth")
                .queryParam("client_id", properties.getAppId())
                .queryParam("redirect_uri", properties.getRedirectUri())
                .queryParam("state", state)
                .queryParam("scope", FacebookConstants.LOGIN_SCOPES)
                .build()
                .toUriString();
        return new FacebookOAuthUrlResponse(url, state);
    }
}
