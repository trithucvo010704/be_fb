package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacebookPostRequest {

    @NotBlank(message = "Page ID không được để trống")
    private String pageId;
    private String content;
    private String link;

    @NotNull(message = "Vị trí đăng bài (postRole) không được để trống")
    private FbPost.PostRole postRole;
    private List<MediaItem> media;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime scheduledTime;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MediaItem {
        @NotBlank(message = "Media URL không được để trống")
        private String url;
        
        @NotNull(message = "Media type không được để trống")
        private FbPostMedia.MediaType type;
    }
}
