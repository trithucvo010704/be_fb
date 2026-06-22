package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbMessageEvent {
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("recipient_id")
    private String recipientId;

    @JsonProperty("message_content")
    private String content;

    @JsonProperty("message_type")
    private MessageType type;

    @JsonProperty("timestamp")
    private Long timestamp;
}
