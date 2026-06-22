package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbUserProfileResponse;

@HttpExchange
public interface FacebookUserGateway {

    @GetExchange("/me")
    FbUserProfileResponse getMe(
            @RequestParam("fields") String fields,
            @RequestParam("access_token") String accessToken
    );
}
