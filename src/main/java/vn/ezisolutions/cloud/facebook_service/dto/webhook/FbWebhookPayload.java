package vn.ezisolutions.cloud.facebook_service.dto.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbWebhookPayload {
    private String object;
    private List<FbWebhookEntry> entry;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookEntry {
        private String id;
        private Long time;
        private List<FbWebhookMessaging> messaging;
        private List<FbWebhookChange> changes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookMessaging {
        private FbWebhookSender sender;
        private FbWebhookRecipient recipient;
        private Long timestamp;
        private FbWebhookMessage message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookSender {
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookRecipient {
        private String id;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookMessage {
        private String mid;
        private String text;
        @JsonProperty("is_echo")
        private Boolean isEcho;
        private List<FbWebhookAttachment> attachments;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookAttachment {
        private String type;
        private FbWebhookAttachmentPayload payload;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookAttachmentPayload {
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookChange {
        private String field;
        private FbWebhookValue value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookValue {
        private String item;
        private String verb;
        @JsonProperty("post_id")
        private String postId;
        @JsonProperty("comment_id")
        private String commentId;
        @JsonProperty("parent_id")
        private String parentId;
        private String message;
        @JsonProperty("created_time")
        private Long createdTime;
        private FbWebhookFrom from;
        @JsonProperty("reaction_type")
        private String reactionType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbWebhookFrom {
        private String id;
        private String name;
    }
}
