package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPageInfoResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPagesDataResponse;

@HttpExchange
public interface FacebookPageGateway {

    @GetExchange("/me/accounts")
    FbPagesDataResponse getUserPages(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam("fields") String fields,
            @RequestParam("limit") int limit
    );

    @GetExchange("/{pageId}")
    FbPageInfoResponse getPageDetails(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable("pageId") String pageId,
            @RequestParam("fields") String fields
    );

    @PostExchange("/{pageId}/subscribed_apps")
    void subscribePageWebhook(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @PathVariable("pageId") String pageId,
            @RequestParam("subscribed_fields") String subscribedFields
    );
}
