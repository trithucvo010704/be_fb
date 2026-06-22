package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookMessengerProfileResponse;

@HttpExchange
public interface FacebookMessengerProfileGateway {

    @GetExchange("/{psid}")
    FacebookMessengerProfileResponse getProfile(
            @PathVariable("psid") String psid,
            @RequestParam("fields") String fields,
            @RequestParam("access_token") String accessToken
    );
}

