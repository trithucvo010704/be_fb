package vn.ezisolutions.cloud.facebook_service.dto.response;

public record FacebookOAuthCallbackResponse(
        String code,
        String state,
        String error,
        String errorDescription
) {
}
