package vn.ezisolutions.cloud.facebook_service.core.exceptions;

import lombok.Getter;
import vn.ezisolutions.cloud.facebook_service.core.FacebookErrorCategory;
import vn.ezisolutions.cloud.facebook_service.core.FacebookErrorCode;

@Getter
public class FacebookApiException extends RuntimeException {

    private final int fbErrorCode;
    private final int fbErrorSubcode;
    private final String fbTraceId;
    private final FacebookErrorCode mappedError;
    private final FacebookErrorCategory category;
    private final int httpStatus;

    public FacebookApiException(int code, int subcode, String message, String traceId) {
        super(message);
        this.fbErrorCode    = code;
        this.fbErrorSubcode = subcode;
        this.fbTraceId      = traceId;
        this.mappedError    = FacebookErrorCode.from(code, subcode);
        this.category       = mappedError.getCategory();
        this.httpStatus     = resolveHttpStatus(this.category);
    }

    public String getUserFriendlyMessage() {
        return mappedError.getUserMessage();
    }

    public boolean isRetryable() {
        return mappedError.getRetryAfterMs() > 0;
    }

    public long getRetryAfterMs() {
        return mappedError.getRetryAfterMs();
    }

    public boolean requiresReauth() {
        return category == FacebookErrorCategory.AUTH;
    }

    public boolean isClientError() {
        return category == FacebookErrorCategory.VALIDATION
                || category == FacebookErrorCategory.ASSET
                || category == FacebookErrorCategory.PAGE;
    }

    private static int resolveHttpStatus(FacebookErrorCategory category) {
        return switch (category) {
            case AUTH                    -> 401;
            case PERMISSION, ACCOUNT     -> 403;
            case VALIDATION, ASSET, PAGE -> 400;
            case RATE_LIMIT              -> 429;
            case AD_REVIEW               -> 422;
            default                      -> 500;
        };
    }
}
