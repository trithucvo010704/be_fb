package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbReactionEvent {

    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("sender_name")
    private String senderName;

    @JsonProperty("post_id")
    private String postId;

    @JsonProperty("comment_id")
    private String commentId;

    @JsonProperty("reaction_type")
    private String reactionType;

    @JsonProperty("verb")
    private String verb;

    @JsonProperty("timestamp")
    private Long timestamp;
}
