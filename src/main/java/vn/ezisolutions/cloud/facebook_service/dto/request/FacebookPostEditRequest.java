package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookPostEditRequest {
    private String content;
    private String link;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;
    private List<MediaItem> media;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {
        private String url;
        private FbPostMedia.MediaType type;
    }
}
