package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FbSendMessageRequest(
    @JsonProperty("messaging_type") String messagingType,
    Recipient recipient,
    Message message
) {
    public static FbSendMessageRequest ofText(String recipientId, String text) {
        return new FbSendMessageRequest(
                "RESPONSE",
                new Recipient(recipientId, null),
                new Message(text, null)
        );
    }

    public static FbSendMessageRequest ofCommentReply(String commentId, String text) {
        return new FbSendMessageRequest(
                "RESPONSE",
                new Recipient(null, commentId),
                new Message(text, null)
        );
    }

    public static FbSendMessageRequest ofAttachment(String recipientId, String type, String mediaUrl) {
        return new FbSendMessageRequest(
                "RESPONSE",
                new Recipient(recipientId, null),
                new Message(
                        null,
                        new Attachment(type, new Payload(mediaUrl, true))
                )
        );
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Recipient(
        String id,
        @JsonProperty("comment_id") String commentId
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Message(
        String text,
        Attachment attachment
    ) {}

    public record Attachment(
        String type,
        Payload payload
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Payload(
        String url,
        @JsonProperty("is_reusable") Boolean isReusable
    ) {}
}
