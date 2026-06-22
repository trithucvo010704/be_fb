package vn.ezisolutions.cloud.facebook_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class FacebookPostResponse {
    private String message;
    private String postId;
    private String fbPostId;
    private String status;
}
