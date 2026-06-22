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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FbPostStoryStrategy implements IFbPublishStrategy {
    private static final Logger log = LoggerFactory.getLogger(FbPostStoryStrategy.class);
    private final FacebookPublishClientService clientService;

    @Override
    public String publish(FbPost post, List<FbPostMedia> mediaList, String token, String fbPageId) throws CustomException {
        if (mediaList == null || mediaList.isEmpty()) {
            throw new CustomException(400, "Story bắt buộc phải có Media");
        }
        FbPostMedia media = mediaList.get(0);
        if (media.getMediaType() == FbPostMedia.MediaType.VIDEO) {
            return handleVideoStory(media, token, fbPageId);
        } else {
            return handlePhotoStory(media, token, fbPageId);
        }
    }

    @Override
    public String update(FbPost post, List<FbPostMedia> mediaList, String token) throws CustomException {
        log.warn("Facebook Stories không hỗ trợ cập nhật.");
        return post.getFbPostId();
    }

    private String handlePhotoStory(FbPostMedia media, String token, String fbPageId) {
        log.info("Đang đăng Photo Story lên Page: {}", fbPageId);

        Map<String, Object> payload = new HashMap<>();
        payload.put(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        payload.put("url", media.getMediaUrl());
        return clientService.postToGraph("/" + fbPageId + "/photo_stories", payload);
    }

    private String handleVideoStory(FbPostMedia media, String token, String fbPageId) throws CustomException {
        log.info("Bắt đầu quy trình đăng Video Story...");

        MultiValueMap<String, String> startParams = new LinkedMultiValueMap<>();
        startParams.add(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        startParams.add(FacebookConstants.GraphApi.UPLOAD_PHASE, "start");
        Map<String, Object> session = clientService.postFormData("/" + fbPageId + "/video_stories", startParams);
        String videoId = session.get(FacebookConstants.GraphApi.VIDEO_ID).toString();
        String uploadUrl = session.get("upload_url").toString();
        String filePath = media.getMediaUrl();
        log.info("Story Session Init thành công. VideoID: {}", videoId);
        if (filePath.startsWith("http")) {
            clientService.uploadVideoByUrl(uploadUrl, filePath, token);
        } else {
            clientService.uploadVideoBinary(uploadUrl, filePath, token);
        }

        MultiValueMap<String, String> finishParams = new LinkedMultiValueMap<>();
        finishParams.add(FacebookConstants.GraphApi.ACCESS_TOKEN, token);
        finishParams.add(FacebookConstants.GraphApi.VIDEO_ID, videoId);
        finishParams.add(FacebookConstants.GraphApi.UPLOAD_PHASE, "finish");
        Map<String, Object> result = clientService.postFormData("/" + fbPageId + "/video_stories", finishParams);
        if (result.containsKey("post_id")) {
            return result.get("post_id").toString();
        }
        if (result.containsKey("id")) {
            return result.get("id").toString();
        }
        throw new CustomException(500, "Không nhận được ID bài viết Story sau khi Finish");
    }
}
