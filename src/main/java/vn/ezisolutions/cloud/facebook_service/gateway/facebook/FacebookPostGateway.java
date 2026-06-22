package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbEngagementSummaryResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbInsightResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPostSyncListResponse;

@HttpExchange
public interface FacebookPostGateway {

    @GetExchange("/{pageId}/posts")
    FbPostSyncListResponse getPagePosts(
            @PathVariable("pageId") String pageId,
            @RequestParam("fields") String fields,
            @RequestParam("limit") int limit,
            @RequestParam("access_token") String accessToken,
            @RequestParam(value = "after", required = false) String after
    );

    @GetExchange("/{postId}/insights")
    FbInsightResponse getPostInsights(
            @PathVariable("postId") String postId,
            @RequestParam("metric") String metric,
            @RequestParam("period") String period,
            @RequestParam("access_token") String accessToken
    );

    @GetExchange("/{postId}")
    FbEngagementSummaryResponse getPostEngagementSummary(
            @PathVariable("postId") String postId,
            @RequestParam("fields") String fields,
            @RequestParam("access_token") String accessToken
    );
}
