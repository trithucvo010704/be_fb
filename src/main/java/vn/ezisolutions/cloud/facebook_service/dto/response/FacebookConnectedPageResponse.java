package vn.ezisolutions.cloud.facebook_service.dto.response;

import java.util.List;
import java.util.UUID;

public record FacebookConnectedPageResponse(
        UUID id,
        String fbPageId,
        String pageName,
        String category,
        String tokenStatus,
        String connectionStatus,
        Boolean webhookSubscribed,
        List<String> grantedPermissions,
        List<String> missingPermissions
) {
}
