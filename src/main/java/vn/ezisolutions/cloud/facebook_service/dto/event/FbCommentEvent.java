package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbCommentEvent {

    @JsonProperty("event_type")
    private String eventType;

    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("data")
    private CommentData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class CommentData {

        @JsonProperty("fb_page_id")
        private String fbPageId;

        @JsonProperty("fb_post_id")
        private String fbPostId;

        @JsonProperty("fb_comment_id")
        private String fbCommentId;

        @JsonProperty("parent_id")
        private String parentId;

        @JsonProperty("message")
        private String message;

        @JsonProperty("created_time")
        private Long createdTime;

        @JsonProperty("sender")
        private Sender sender;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Sender {
        @JsonProperty("id")
        private String id;
        @JsonProperty("name")
        private String name;
    }
}
