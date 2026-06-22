package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostLog;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostLogRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostMediaRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy.FbPostFeedStrategy;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy.FbPostReelStrategy;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy.FbPostStoryStrategy;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy.IFbPublishStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FbPublishWorkerService {
    private static final Logger log = LoggerFactory.getLogger(FbPublishWorkerService.class);

    private final FbPostRepository fbPostRepository;
    private final FbPageRepository fbPageRepository;
    private final FbPostLogRepository fbPostLogRepository;
    private final FbPostMediaRepository fbPostMediaRepository;

    private final FbPostFeedStrategy feedStrategy;
    private final FbPostStoryStrategy storyStrategy;
    private final FbPostReelStrategy reelStrategy;

    public void processPost(FbPost post) {
        log.info("Bắt đầu xử lý Post ID: {}", post.getId());
        try {
            FbPage fbPage = fbPageRepository.findByFbPageId(post.getPageId())
                    .orElseThrow(() -> new CustomException(404, "Page ID " + post.getPageId() + " không tồn tại"));
            if (fbPage.getTokenStatus() != FbPage.TokenStatus.ACTIVE) {
                throw new CustomException(400, "Token của Page đang INACTIVE hoặc hết hạn.");
            }
            List<FbPostMedia> mediaList = fbPostMediaRepository.findByPostIdOrderByMediaOrderAsc(post.getId());
            IFbPublishStrategy strategy = selectStrategy(post.getPostRole());
            boolean isUpdate = post.getFbPostId() != null && !post.getFbPostId().isEmpty();
            String fbPostId;
            if (isUpdate) {
                log.info(" Phát hiện bài CŨ (ID FB: {}). Đang thực hiện CẬP NHẬT", post.getFbPostId());
                fbPostId = strategy.update(post, mediaList, fbPage.getPageAccessToken());
            } else {
                log.info(" Phát hiện bài MỚI. Đang thực hiện ĐĂNG");
                fbPostId = strategy.publish(post, mediaList, fbPage.getPageAccessToken(), fbPage.getFbPageId());
            }
            handleSuccess(post, fbPostId, mediaList);

        } catch (Exception e) {
            handleError(post, e);
        }
    }

    private IFbPublishStrategy selectStrategy(FbPost.PostRole role) {
        return switch (role) {
            case STORY -> storyStrategy;
            case REELS -> reelStrategy;
            default -> feedStrategy;
        };
    }

    private void handleSuccess(FbPost post, String fbPostId, List<FbPostMedia> mediaList) {
        log.info(">>> ĐĂNG THÀNH CÔNG! FB_ID: {}", fbPostId);
        post.setFbPostId(fbPostId);
        post.setStatus(FbPost.PostStatus.SUCCESS);
        post.setPostedTime(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        fbPostRepository.save(post);
        if (mediaList != null && !mediaList.isEmpty()) {
            mediaList.forEach(m -> m.setUploadStatus(FbPostMedia.UploadStatus.UPLOADED));
            fbPostMediaRepository.saveAll(mediaList);
        }
        saveLog(post.getId(), Map.of("id", fbPostId), FbPostLog.LogResult.SUCCESS);
    }

    private void handleError(FbPost post, Exception e) {
        String errorMessage = e.getMessage();
        if (e instanceof FacebookApiException fae) {
            log.error("CHI TIẾT LỖI FACEBOOK TRẢ VỀ: code={}, subcode={}, message={}", fae.getFbErrorCode(), fae.getFbErrorSubcode(), fae.getMessage());
            errorMessage = "FB Error: " + fae.getUserFriendlyMessage();
        } else if (e instanceof CustomException ce) {
            log.error("CHI TIẾT LỖI FACEBOOK TRẢ VỀ: {}", ce.getMessage());
            errorMessage = "FB Error: " + ce.getMessage();
        } else {
            log.error("ĐĂNG THẤT BẠI Post ID: {} | Lỗi: {}", post.getId(), e.getMessage());
        }

        post.setStatus(FbPost.PostStatus.FAILED);
        post.setUpdatedAt(LocalDateTime.now());
        fbPostRepository.save(post);

        saveLog(post.getId(), Map.of("error", errorMessage), FbPostLog.LogResult.ERROR);
    }

    private void saveLog(UUID postId, Map<String, Object> payload, FbPostLog.LogResult result) {
        try {
            FbPostLog logEntry = FbPostLog.builder()
                    .postId(postId)
                    .responsePayload(payload)
                    .result(result)
                    .createdAt(LocalDateTime.now())
                    .build();
            fbPostLogRepository.save(logEntry);
        } catch (Exception ex) {
            log.error("Không thể lưu log cho Post {}: {}", postId, ex.getMessage());
        }
    }
}
