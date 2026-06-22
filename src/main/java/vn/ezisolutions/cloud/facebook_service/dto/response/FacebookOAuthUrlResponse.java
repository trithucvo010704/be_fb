package vn.ezisolutions.cloud.facebook_service.dto.response;

public record FacebookOAuthUrlResponse(
        String url,
        String state
) {
}
