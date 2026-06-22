package vn.ezisolutions.cloud.facebook_service.services.facebook.sync;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.event.KafkaSystemEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbSyncJobEvent;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbEngagementSummaryResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbInsightResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostInsight;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.listeners.KafkaProducer;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostInsightRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookGraphClientService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookSyncInsightService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookSyncInsightService.class);

    private static final String METRIC_POST_MEDIA_VIEW = "post_media_view";
    private static final String METRIC_POST_ENGAGED_USERS = "post_engaged_users";
    private static final long SYNC_DELAY_MS = 300L;

    private final FbPostInsightRepository fbPostInsightRepository;
    private final FacebookTokenService facebookTokenService;
    private final FacebookGraphClientService graphClientService;
    private final FbPageRepository fbPageRepository;
    private final FbUserRepository fbUserRepository;
    private final KafkaProducer kafkaProducer;
    private final FbUserPageRepository fbUserPageRepository;

    public void triggerSyncInsights(String ownerId, String pageId, List<String> postIds) throws CustomException {
        logger.info("triggerSyncInsights start - ownerId: {}, pageId: {}, postIds: {}", ownerId, pageId, postIds.size());
        FbPage page = fbPageRepository.findByFbPageId(pageId)
                .orElseThrow(() -> new CustomException(404, "Không tìm thấy Fanpage."));
        FbUser fbUser = fbUserRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new CustomException(400, "Tài khoản hệ thống chưa liên kết với Facebook."));

        if (!fbUserPageRepository.existsByFbUserIdAndFbPageId(fbUser.getFbUserId(), page.getFbPageId())) {
            throw new CustomException(403, "Bạn không có quyền thao tác trên Fanpage này.");
        }

        sendSyncEvent(ownerId, pageId, postIds);
    }

    private void sendSyncEvent(String ownerId, String pageId, List<String> postIds) throws CustomException {
        try {
            FbSyncJobEvent eventPayload = FbSyncJobEvent.builder()
                    .ownerId(ownerId)
                    .pageId(pageId)
                    .postIds(postIds)
                    .build();
            KafkaSystemEvent<FbSyncJobEvent> event = KafkaSystemEvent.<FbSyncJobEvent>builder()
                    .name(FbActionEvent.SYNC_PAGE_INSIGHTS.getValue())
                    .payload(eventPayload)
                    .build();
            kafkaProducer.sendEvent(KafkaTopics.FACEBOOK_SYNC_REQUEST, pageId, event);
            logger.info("Đã gửi yêu cầu đồng bộ Insight cho {} bài viết vào Kafka", postIds.size());
        } catch (Exception e) {
            logger.error("Lỗi khi đẩy event đồng bộ Insight - pageId: {}, error: {}", pageId, e.getMessage(), e);
            throw new CustomException(500, "Lỗi hệ thống khi khởi tạo tiến trình đồng bộ Insight.");
        }
    }

    public void processSyncInsights(String pageId, List<String> postIds) {
        logger.info("BẮT ĐẦU WORKER KÉO INSIGHT CHO {} BÀI VIẾT (PAGE: {}) ---", postIds.size(), pageId);

        String pageToken = facebookTokenService.getPageAccessToken(pageId);
        if (pageToken == null) {
            logger.error("Dừng kéo Insight: Page {} không có Token hợp lệ", pageId);
            return;
        }

        int successCount = processPosts(postIds, pageId, pageToken);
        logger.info("HOÀN TẤT! Đã kéo thành công Insight và Tương tác cho {}/{} bài viết ---", successCount, postIds.size());
    }

    private int processPosts(List<String> postIds, String pageId, String pageToken) {
        int successCount = 0;
        for (String postId : postIds) {
            try {
                syncSinglePost(postId, pageId, pageToken);
                successCount++;
                Thread.sleep(SYNC_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Sync insight bị interrupt tại postId: {}", postId);
                break;
            } catch (Exception e) {
                logger.error("Lỗi khi kéo Insight cho Post {}: {}", postId, e.getMessage());
            }
        }
        return successCount;
    }

    private void syncSinglePost(String postId, String pageId, String pageToken) {
        var response = graphClientService.getPostInsights(postId, pageToken);
        var summaryResp = graphClientService.getPostEngagementSummary(postId, pageToken);
        UUID postUUID = UUID.fromString(postId);

        FbPostInsight insight = fbPostInsightRepository.findByPostId(postUUID)
                .orElse(FbPostInsight.builder().postId(postUUID).pageId(pageId).build());

        applyMetricData(insight, response);
        applyEngagementData(insight, summaryResp);

        insight.setCollectedAt(LocalDateTime.now());
        fbPostInsightRepository.save(insight);
    }

    private void applyMetricData(FbPostInsight insight, FbInsightResponse response) {
        if (response == null || response.data() == null) {
            return;
        }
        for (var data : response.data()) {
            applyMetricValue(insight, data);
        }
        int totalReach = insight.getTotalReach() != null ? insight.getTotalReach() : 0;
        insight.setPaidReach(0);
        insight.setOrganicReach(totalReach);
    }

    private void applyMetricValue(FbPostInsight insight, FbInsightResponse.InsightData data) {
        if (data.values() == null || data.values().isEmpty()) {
            return;
        }
        Integer metricValue = data.values().get(0).value();

        if (METRIC_POST_MEDIA_VIEW.equals(data.name())) {
            insight.setTotalImpressions(metricValue);
            insight.setTotalReach(metricValue);
        } else if (METRIC_POST_ENGAGED_USERS.equals(data.name())) {
            insight.setEngagedUsers(metricValue);
        }
    }

    private void applyEngagementData(FbPostInsight insight, FbEngagementSummaryResponse summaryResp) {
        if (summaryResp == null) {
            return;
        }
        applyLikeCount(insight, summaryResp);
        applyCommentCount(insight, summaryResp);
        applyShareCount(insight, summaryResp);
    }

    private void applyLikeCount(FbPostInsight insight, FbEngagementSummaryResponse resp) {
        if (resp.likes() != null && resp.likes().summary() != null) {
            insight.setLikeCount(resp.likes().summary().totalCount());
        }
    }

    private void applyCommentCount(FbPostInsight insight, FbEngagementSummaryResponse resp) {
        if (resp.comments() != null && resp.comments().summary() != null) {
            insight.setCommentCount(resp.comments().summary().totalCount());
        }
    }

    private void applyShareCount(FbPostInsight insight, FbEngagementSummaryResponse resp) {
        if (resp.shares() != null && resp.shares().count() != null) {
            insight.setShareCount(resp.shares().count());
        } else {
            insight.setShareCount(0);
        }
    }
}
