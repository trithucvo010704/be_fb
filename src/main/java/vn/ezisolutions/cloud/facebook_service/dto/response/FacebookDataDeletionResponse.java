package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbDataDeletionRequest;

import java.util.UUID;

public record FacebookDataDeletionResponse(
        UUID requestId,
        FbDataDeletionRequest.Status status,
        String confirmationCode,
        String message
) {
}

