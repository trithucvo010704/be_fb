package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookConnectedPageResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthExchangeResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthTokenResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthUrlResponse;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookOAuthGateway;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class FacebookOAuthService {
    private final FacebookProperties properties;
    private final FacebookOAuthGateway oauthGateway;
    private final FacebookPageConnectService pageConnectService;
    private final Map<String, AuthorizedUser> stateOwners = new ConcurrentHashMap<>();

    public FacebookOAuthUrlResponse buildAuthorizationUrl() {
        return buildAuthorizationUrl(null);
    }

    public FacebookOAuthUrlResponse buildAuthorizationUrl(AuthorizedUser owner) {
        String state = UUID.randomUUID().toString();
        if (owner != null) {
            stateOwners.put(state, owner);
        }
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

    public FacebookOAuthExchangeResponse handleCallback(
            String code,
            String state,
            String error,
            String errorDescription
    ) throws CustomException {
        return handleCallback(code, state, error, errorDescription, null);
    }

    public FacebookOAuthExchangeResponse handleCallback(
            String code,
            String state,
            String error,
            String errorDescription,
            AuthorizedUser requestOwner
    ) throws CustomException {
        if (error != null && !error.isBlank()) {
            return FacebookOAuthExchangeResponse.error(code, state, error, errorDescription);
        }
        if (code == null || code.isBlank()) {
            return FacebookOAuthExchangeResponse.error(code, state, "missing_code", "OAuth callback thiếu code");
        }

        FacebookOAuthTokenResponse token = exchangeCode(code);
        AuthorizedUser stateOwner = state == null ? null : stateOwners.remove(state);
        AuthorizedUser owner = requestOwner != null ? requestOwner : stateOwner;
        if (owner == null) {
            return new FacebookOAuthExchangeResponse(code, state, null, null, true, false, List.of());
        }
        List<FacebookConnectedPageResponse> pages = pageConnectService.connect(token.accessToken(), owner);
        return new FacebookOAuthExchangeResponse(code, state, null, null, true, true, pages);
    }

    public FacebookOAuthTokenResponse exchangeCode(String code) throws CustomException {
        try {
            FacebookOAuthTokenResponse response = oauthGateway.exchangeCode(
                    properties.getAppId(),
                    properties.getAppSecret(),
                    properties.getRedirectUri(),
                    code
            );
            if (response == null || response.accessToken() == null || response.accessToken().isBlank()) {
                throw new CustomException(400, "Facebook không trả access token");
            }
            return response;
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(400, "Không exchange được OAuth code: " + e.getMessage());
        }
    }
}
