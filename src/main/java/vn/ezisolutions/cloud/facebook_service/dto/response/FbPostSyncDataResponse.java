package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FbPostSyncDataResponse(
        String id,
        String message,
        @JsonProperty("created_time") String createdTime,
        @JsonProperty("full_picture") String fullPicture,
        @JsonProperty("permalink_url") String permalinkUrl,
        Attachments attachments
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachments(List<AttachmentData> data) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AttachmentData(
            @JsonProperty("media_type") String mediaType,
            Media media,
            @JsonProperty("subattachments") Attachments subattachments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Media(Image image, String source) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Image(String src) {}
}
