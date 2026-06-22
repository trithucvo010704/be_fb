package vn.ezisolutions.cloud.facebook_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbPostResultEvent {
    private String postId;
    private String status;
    private String fbPostId;
    private String message;
    private String timestamp;
}
