package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;

public record FacebookErrorPayload(
    int code,
    int subcode,
    String message
) {
    public static FacebookErrorPayload from(FacebookApiException e) {
        return new FacebookErrorPayload(e.getFbErrorCode(), e.getFbErrorSubcode(), e.getMessage());
    }
}
