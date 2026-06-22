package vn.ezisolutions.cloud.facebook_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbPagePostResponse {
    private String id;
    private String fbPostId;
    private String content;
    private String linkUrl;
    private String status;
    private LocalDateTime createdAt;
    private List<FbPagePostMediaResponse> media;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FbPagePostMediaResponse {
        private String mediaUrl;
        private String thumbnailUrl;
        private String mediaType;
        private int mediaOrder;
    }
}
