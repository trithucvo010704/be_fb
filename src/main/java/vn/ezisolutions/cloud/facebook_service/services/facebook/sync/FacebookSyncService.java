package vn.ezisolutions.cloud.facebook_service.services.facebook.sync;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.event.KafkaSystemEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.core.shared.KafkaTopics;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbSyncJobEvent;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPostSyncDataResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPostSyncListResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.listeners.KafkaProducer;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.*;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookGraphClientService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookSyncService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookSyncService.class);
    
    private final FbPageRepository fbPageRepository;
    private final FbUserRepository fbUserRepository;
    private final FbPostRepository fbPostRepository;
    private final FbPostMediaRepository fbPostMediaRepository;
    private final FacebookTokenService facebookTokenService;
    private final FacebookGraphClientService graphClientService;
    private final KafkaProducer kafkaProducer;
    private final FbUserPageRepository fbUserPageRepository;

    public void triggerSyncHistory(String ownerId, String pageId) throws CustomException {
        logger.info("triggerSyncHistory start - ownerId: {}, pageId: {}", ownerId, pageId);
        FbPage page = fbPageRepository.findByFbPageId(pageId)
                .orElseThrow(() -> new CustomException(404, "Không tìm thấy Fanpage."));
        FbUser fbUser = fbUserRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new CustomException(400, "Tài khoản hệ thống chưa liên kết với Facebook."));

        if (!fbUserPageRepository.existsByFbUserIdAndFbPageId(fbUser.getFbUserId(), page.getFbPageId())) {
            throw new CustomException(403, "Bạn không có quyền thao tác trên Fanpage này.");
        }

        try {
            FbSyncJobEvent eventPayload = FbSyncJobEvent.builder()
                    .ownerId(ownerId)
                    .pageId(pageId)
                    .build();
            KafkaSystemEvent<FbSyncJobEvent> event = KafkaSystemEvent.<FbSyncJobEvent>builder()
                    .name(FbActionEvent.SYNC_PAGE_HISTORY.getValue())
                    .payload(eventPayload)
                    .build();
            kafkaProducer.sendEvent(KafkaTopics.FACEBOOK_SYNC_REQUEST, pageId, event);

            logger.info("Đã gửi yêu cầu đồng bộ lịch sử vào Kafka cho Page: {}", pageId);
        } catch (Exception e) {
            logger.error("Lỗi khi đẩy event đồng bộ - pageId: {}, error: {}", pageId, e.getMessage(), e);
            throw new CustomException(500, "Lỗi hệ thống khi khởi tạo tiến trình đồng bộ.");
        }
    }

    public void processSyncHistory(String pageId) {
        logger.info("processSyncHistory start - pageId: {}", pageId);
        try {
            String pageToken = facebookTokenService.getPageAccessToken(pageId);
            if (pageToken == null) {
                logger.error("Dừng đồng bộ: Page {} không có Token hợp lệ", pageId);
                return;
            }
            String nextCursor = null;
            int totalSynced = 0;
            int pageCount = 1;
            do {
                logger.info("Đang kéo Page thứ {} (Cursor: {})", pageCount, nextCursor);
                FbPostSyncListResponse response = graphClientService.getPagePosts(pageId, pageToken, 100, nextCursor);

                if (response == null || response.data() == null || response.data().isEmpty()) {
                    break;
                }
                for (FbPostSyncDataResponse fbData : response.data()) {
                    upsertPostAndMedia(fbData, pageId);
                    totalSynced++;
                }
                nextCursor = null;
                if (response.paging() != null && response.paging().cursors() != null && response.paging().next() != null) {
                    nextCursor = response.paging().cursors().after();
                }
                pageCount++;
                Thread.sleep(500);
            } while (nextCursor != null);
            logger.info("HOÀN TẤT! Đã đồng bộ tổng cộng {} bài viết cho Page: {} ---", totalSynced, pageId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Sync history bị interrupt cho Page: {}", pageId);
        } catch (FacebookApiException e) {
            logger.error("Lỗi đồng bộ lịch sử Page: {} từ Facebook: code={}, subcode={}, message={}", pageId, e.getFbErrorCode(), e.getFbErrorSubcode(), e.getMessage());
        } catch (Exception e) {
            logger.error("Lỗi trong quá trình Worker kéo dữ liệu cho Page: {} - error: {}", pageId, e.getMessage(), e);
        }
    }

    private void upsertPostAndMedia(FbPostSyncDataResponse fbData, String pageId) {
        FbPost fbPost = fbPostRepository.findByFbPostId(fbData.id()).orElse(new FbPost());
        fbPost.setFbPostId(fbData.id());
        fbPost.setPageId(pageId);
        fbPost.setContent(fbData.message() != null ? fbData.message() : "");
        if (fbData.permalinkUrl() != null) {
            fbPost.setLinkUrl(fbData.permalinkUrl());
        }
        fbPost.setSource(FbPost.Source.FACEBOOK);
        fbPost.setStatus(FbPost.PostStatus.PUBLISHED);
        applyCreatedTime(fbPost, fbData);

        FbPost savedPost = fbPostRepository.save(fbPost);
        upsertMediaForPost(savedPost, fbData);
    }

    private void applyCreatedTime(FbPost fbPost, FbPostSyncDataResponse fbData) {
        if (fbPost.getCreatedAt() != null || fbData.createdTime() == null) {
            return;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(fbData.createdTime(), java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
            fbPost.setCreatedAt(odt.toLocalDateTime());
        } catch (java.time.format.DateTimeParseException ex) {
            fbPost.setCreatedAt(LocalDateTime.now());
        }
    }

    private void upsertMediaForPost(FbPost savedPost, FbPostSyncDataResponse fbData) {
        if (fbData.attachments() == null || fbData.attachments().data() == null || fbData.attachments().data().isEmpty()) {
            return;
        }
        UUID savedPostId = savedPost.getId();
        List<FbPostMedia> existingMedia = fbPostMediaRepository.findByPostId(savedPostId);
        if (existingMedia != null && !existingMedia.isEmpty()) {
            return;
        }

        var attachment = fbData.attachments().data().get(0);
        if (attachment.subattachments() != null && attachment.subattachments().data() != null) {
            int order = 0;
            for (var sub : attachment.subattachments().data()) {
                saveMediaItem(sub.mediaType(), sub.media(), savedPostId, order++);
            }
        } else {
            saveMediaItem(attachment.mediaType(), attachment.media(), savedPostId, 0);
        }
    }

    private void saveMediaItem(String mediaType, FbPostSyncDataResponse.Media mediaInfo, UUID savedPostId, int order) {
        if (mediaInfo == null) return;

        String imageUrl = mediaInfo.image() != null ? mediaInfo.image().src() : null;
        String videoUrl = mediaInfo.source();

        FbPostMedia media = null;
        if ("video".equals(mediaType) && videoUrl != null) {
            media = FbPostMedia.builder().postId(savedPostId).mediaUrl(videoUrl).thumbnailUrl(imageUrl)
                    .mediaType(FbPostMedia.MediaType.VIDEO).mediaOrder(order).uploadStatus(FbPostMedia.UploadStatus.UPLOADED).build();
        } else if (("photo".equals(mediaType) || "album".equals(mediaType)) && imageUrl != null) {
            media = FbPostMedia.builder().postId(savedPostId).mediaUrl(imageUrl)
                    .mediaType(FbPostMedia.MediaType.IMAGE).mediaOrder(order).uploadStatus(FbPostMedia.UploadStatus.UPLOADED).build();
        }

        if (media != null) {
            media.setCreatedAt(LocalDateTime.now());
            fbPostMediaRepository.save(media);
        }
    }
}
