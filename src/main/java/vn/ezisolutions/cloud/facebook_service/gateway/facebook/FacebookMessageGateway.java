package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse;

@HttpExchange
public interface FacebookMessageGateway {

    @PostExchange("/me/messages")
    FbMessageResponse sendMessage(
            @RequestParam("access_token") String accessToken,
            @RequestBody FbSendMessageRequest body
    );
}
