package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePostMedia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record FacebookPagePostResponse(
        UUID id,
        UUID pageId,
        String fbPostId,
        String content,
        FbPagePost.Status status,
        String errorMessage,
        LocalDateTime publishedAt,
        String createdBy,
        List<Media> media
) {
    public static FacebookPagePostResponse from(FbPagePost post, List<FbPagePostMedia> media) {
        return new FacebookPagePostResponse(
                post.getId(),
                post.getPageId(),
                post.getFbPostId(),
                post.getContent(),
                post.getStatus(),
                post.getErrorMessage(),
                post.getPublishedAt(),
                post.getCreatedBy(),
                media == null ? List.of() : media.stream().map(Media::from).toList()
        );
    }

    public record Media(
            UUID id,
            FbPagePostMedia.MediaType mediaType,
            String mediaUrl,
            String facebookMediaId,
            Integer mediaOrder
    ) {
        static Media from(FbPagePostMedia media) {
            return new Media(
                    media.getId(),
                    media.getMediaType(),
                    media.getMediaUrl(),
                    media.getFacebookMediaId(),
                    media.getMediaOrder()
            );
        }
    }
}

