package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbEngagementSummaryResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbInsightResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPostSyncListResponse;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookPostGateway;

@Service
@RequiredArgsConstructor
public class FacebookGraphClientService {
    private static final Logger log = LoggerFactory.getLogger(FacebookGraphClientService.class);
    private final FacebookPostGateway postClient;

    public FbPostSyncListResponse getPagePosts(String pageId, String pageToken, int limit, String nextCursor) {
        try {
            return postClient.getPagePosts(
                    pageId,
                    FacebookConstants.ENDPOINT_POST_SYNC,
                    limit,
                    pageToken,
                    nextCursor
            );
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("[FB-GRAPH-CLIENT] Lỗi lấy posts của Page {} - error: {}", pageId, e.getMessage(), e);
            throw new FacebookApiException(-1, 0, "Lỗi mạng hoặc lỗi kết nối Facebook: " + e.getMessage(), null);
        }
    }

    public FbInsightResponse getPostInsights(String postId, String pageToken) {
        try {
            log.info("Đang lấy Insight cho Post: {}", postId);
            return postClient.getPostInsights(
                    postId,
                    FacebookConstants.ENDPOINT_POST_INSIGHT,
                    "lifetime",
                    pageToken
            );
        } catch (FacebookApiException e) {
            log.warn("Bài viết {} không hỗ trợ lấy Insight hoặc bị lỗi: code={}, message={}", postId, e.getFbErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Lỗi hệ thống khi lấy Insight bài viết {}: {}", postId, e.getMessage());
            return null;
        }
    }

    public FbEngagementSummaryResponse getPostEngagementSummary(String postId, String pageToken) {
        try {
            log.info("Đang lấy Summary tương tác cho Post: {}", postId);
            return postClient.getPostEngagementSummary(
                    postId,
                    FacebookConstants.ENDPOINT_POST_ENGAGEMENT_SUMMARY,
                    pageToken
            );
        } catch (FacebookApiException e) {
            log.warn("Không thể lấy summary tương tác cho bài viết {} do lỗi Facebook: code={}, message={}", postId, e.getFbErrorCode(), e.getMessage());
            return null;
        } catch (Exception e) {
            log.warn("Không thể lấy summary tương tác cho bài viết {}: {}", postId, e.getMessage());
            return null;
        }
    }
}
