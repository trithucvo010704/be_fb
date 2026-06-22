package vn.ezisolutions.cloud.facebook_service.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record FacebookPublishPostRequest(
        @Size(max = 63206, message = "content vượt quá giới hạn Facebook cho Page feed")
        String content,

        List<String> imageUrls
) {
    public boolean hasImages() {
        return imageUrls != null && imageUrls.stream().anyMatch(url -> url != null && !url.isBlank());
    }
}

