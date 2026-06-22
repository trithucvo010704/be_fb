package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthTokenResponse;

@HttpExchange
public interface FacebookOAuthGateway {

    @GetExchange("/oauth/access_token")
    FacebookOAuthTokenResponse exchangeCode(
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam("code") String code
    );
}

