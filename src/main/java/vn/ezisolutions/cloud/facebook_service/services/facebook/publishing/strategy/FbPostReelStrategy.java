package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.FacebookPublishClientService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FbPostReelStrategy implements IFbPublishStrategy {
    private static final Logger log = LoggerFactory.getLogger(FbPostReelStrategy.class);
    private final FacebookPublishClientService clientService;

    @Override
    public String publish(FbPost post, List<FbPostMedia> mediaList, String token, String fbPageId) throws CustomException {
        if (mediaList == null || mediaList.isEmpty()) {
            throw new CustomException(400, "Reels yêu cầu Video");
        }

        FbPostMedia media = mediaList.get(0);

        if (media.getMediaType() != FbPostMedia.MediaType.VIDEO) {
            throw new CustomException(400, "Đăng Reels bắt buộc phải là định dạng Video.");
        }

        MultiValueMap<String, String> startParams = new LinkedMultiValueMap<>();
        startParams.add(FacebookConstants.GraphApi.UPLOAD_PHASE, "start");
        startParams.add(FacebookConstants.GraphApi.ACCESS_TOKEN, token);

        Map<String, Object> session = clientService.postFormData("/" + fbPageId + "/video_reels", startParams);

        if (session == null || !session.containsKey(FacebookConstants.GraphApi.VIDEO_ID) || !session.containsKey("upload_url")) {
            throw new CustomException(500, "Không khởi tạo được Session Reels của Facebook (Thiếu video_id hoặc upload_url).");
        }

        String videoId = session.get(FacebookConstants.GraphApi.VIDEO_ID).toString();
        String uploadUrl = session.get("upload_url").toString();

        uploadVideo(media, uploadUrl, token);

        return finishReelPublish(post, videoId, token, fbPageId);
    }

    @Override
    public String update(FbPost post, List<FbPostMedia> mediaList, String token) throws CustomException {
        Map<String, Object> payload = Map.of(
                FacebookConstants.GraphApi.ACCESS_TOKEN, token,
                FacebookConstants.GraphApi.DESCRIPTION, post.getContent()
        );
        if (clientService.updatePost(post.getFbPostId(), payload)) {
            return post.getFbPostId();
        }
        throw new CustomException(500, "Cập nhật Reels thất bại");
    }

    private void uploadVideo(FbPostMedia media, String uploadUrl, String token) throws CustomException {
        try {
            if (media.getMediaUrl().startsWith("http")) {
                clientService.uploadVideoByUrl(uploadUrl, media.getMediaUrl(), token);
            } else {
                clientService.uploadVideoBinary(uploadUrl, media.getMediaUrl(), token);
            }
        } catch (Exception e) {
            log.error("Lỗi khi tải file video Reels lên Facebook: {}", e.getMessage());
            throw new CustomException(500, "Tải video Reels lên Facebook thất bại: " + e.getMessage());
        }
    }

    private String finishReelPublish(FbPost post, String videoId, String token, String fbPageId) throws CustomException {
        MultiValueMap<String, String> finishParams = new LinkedMultiValueMap<>();
        finishParams.add(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        finishParams.add(FacebookConstants.GraphApi.VIDEO_ID, videoId);
        finishParams.add(FacebookConstants.GraphApi.UPLOAD_PHASE, "finish");

        if (post.getContent() != null && !post.getContent().isEmpty()) {
            finishParams.add(FacebookConstants.GraphApi.DESCRIPTION, post.getContent());
        }

        handleScheduling(finishParams, post);

        Map<String, Object> result = clientService.postFormData("/" + fbPageId + "/video_reels", finishParams);
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            return videoId;
        }

        throw new CustomException(500, "Publish Reels thất bại");
    }

    private void handleScheduling(MultiValueMap<String, String> params, FbPost post) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduled = post.getScheduledTime();
        if (scheduled == null || scheduled.isBefore(now.plusMinutes(1))) {
            params.add(FacebookConstants.GraphApi.VIDEO_STATE, "PUBLISHED");
        } else {
            params.add(FacebookConstants.GraphApi.VIDEO_STATE, "SCHEDULED");
            params.add(FacebookConstants.GraphApi.SCHEDULED_PUBLISH_TIME,
                    String.valueOf(scheduled.atZone(ZoneId.systemDefault()).toEpochSecond()));
        }
    }
}
