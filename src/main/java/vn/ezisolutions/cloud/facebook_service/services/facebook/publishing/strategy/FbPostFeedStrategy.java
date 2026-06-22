package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.FacebookPublishClientService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FbPostFeedStrategy implements IFbPublishStrategy {
    private static final Logger log = LoggerFactory.getLogger(FbPostFeedStrategy.class);
    private final FacebookPublishClientService clientService;

    @Override
    public String publish(FbPost post, List<FbPostMedia> mediaList, String token, String fbPageId)
            throws CustomException {
        if (mediaList == null || mediaList.isEmpty()) {
            return publishTextLink(post, token, fbPageId);
        }

        if (mediaList.size() == 1 && mediaList.get(0).getMediaType() == FbPostMedia.MediaType.VIDEO) {
            return publishVideoPost(post, mediaList.get(0), token, fbPageId);
        }

        List<String> mediaIds = new ArrayList<>();
        for (FbPostMedia media : mediaList) {
            if (media.getMediaType() == FbPostMedia.MediaType.VIDEO) {
                throw new CustomException(400, "Bài viết Feed chứa nhiều file không được phép kèm Video. Vui lòng đăng Video riêng lẻ hoặc đăng qua Reels/Story.");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
            payload.put("url", media.getMediaUrl());
            payload.put(FacebookConstants.GraphApi.PUBLISHED, false);

            try {
                String id = clientService.postToGraph("/" + fbPageId + "/photos", payload);
                mediaIds.add(id);
            } catch (FacebookApiException e) {
                log.error("Lỗi upload ảnh tạm thời lên Facebook. URL: {}, Error: {}", media.getMediaUrl(), e.getMessage());
                throw new CustomException(500, "Lỗi upload ảnh: " + e.getUserFriendlyMessage());
            }
        }

        return publishFeedWithMedia(post, mediaIds, token, fbPageId);
    }

    @Override
    public String update(FbPost post, List<FbPostMedia> mediaList, String token) throws CustomException {
        Map<String, Object> payload = new HashMap<>();
        payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        payload.put(FacebookConstants.GraphApi.MESSAGE, post.getContent());

        if (clientService.updatePost(post.getFbPostId(), payload)) {
            return post.getFbPostId();
        }
        throw new CustomException(500, "Cập nhật Feed thất bại");
    }

    private String publishVideoPost(FbPost post, FbPostMedia media, String token, String fbPageId) {
        log.info("Đang đăng Video Post lên Feed của Page: {}", fbPageId);
        Map<String, Object> payload = new HashMap<>();
        payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        if (post.getContent() != null && !post.getContent().isEmpty()) {
            // Lưu ý: Đối với endpoint /videos, Facebook sử dụng trường "description" để hiển thị caption bài viết thay vì "message"
            payload.put(FacebookConstants.GraphApi.DESCRIPTION, post.getContent());
        }
        payload.put(FacebookConstants.GraphApi.FILE_URL, media.getMediaUrl());

        handleScheduling(payload, post);
        return clientService.postToGraph("/" + fbPageId + "/videos", payload);
    }

    private String publishTextLink(FbPost post, String token, String fbPageId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        payload.put(FacebookConstants.GraphApi.MESSAGE, post.getContent());
        if (post.getLinkUrl() != null && !post.getLinkUrl().isEmpty()) {
            payload.put("link", post.getLinkUrl());
        }
        handleScheduling(payload, post);
        return clientService.postToGraph("/" + fbPageId + "/feed", payload);
    }

    private String publishFeedWithMedia(FbPost post, List<String> mediaIds, String token, String fbPageId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        payload.put(FacebookConstants.GraphApi.MESSAGE, post.getContent());

        List<Map<String, String>> attachments = mediaIds.stream()
                .map(id -> Map.of("media_fbid", id))
                .toList();
        payload.put("attached_media", attachments);

        handleScheduling(payload, post);
        return clientService.postToGraph("/" + fbPageId + "/feed", payload);
    }

    private void handleScheduling(Map<String, Object> payload, FbPost post) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = post.getScheduledTime();
        if (scheduled == null || scheduled.isBefore(now.plusMinutes(1))) {
            payload.put(FacebookConstants.GraphApi.PUBLISHED, true);
        } else {
            payload.put(FacebookConstants.GraphApi.PUBLISHED, false);
            payload.put(FacebookConstants.GraphApi.SCHEDULED_PUBLISH_TIME, scheduled.atZone(ZoneId.systemDefault()).toEpochSecond());
        }
    }
}
