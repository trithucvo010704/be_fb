package vn.ezisolutions.cloud.facebook_service.dto.request;

public record FacebookDataDeletionRequest(
        String requesterEmail,
        String fbUserId,
        String fbPageId,
        String signedRequest,
        String note
) {
}

