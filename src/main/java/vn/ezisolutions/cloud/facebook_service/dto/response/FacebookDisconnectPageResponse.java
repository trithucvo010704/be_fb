package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;

import java.time.LocalDateTime;
import java.util.UUID;

public record FacebookDisconnectPageResponse(
        UUID pageId,
        String fbPageId,
        FbPage.ConnectionStatus connectionStatus,
        FbPage.TokenStatus tokenStatus,
        LocalDateTime disconnectedAt
) {
    public static FacebookDisconnectPageResponse from(FbPage page) {
        return new FacebookDisconnectPageResponse(
                page.getId(),
                page.getFbPageId(),
                page.getConnectionStatus(),
                page.getTokenStatus(),
                page.getDisconnectedAt()
        );
    }
}

