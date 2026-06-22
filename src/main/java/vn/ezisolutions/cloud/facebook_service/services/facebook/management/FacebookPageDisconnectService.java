package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDisconnectPageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookDisconnectPageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageDisconnectLog;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageDisconnectLogRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FacebookPageDisconnectService {

    private final FacebookPageAccessGuard pageAccessGuard;
    private final FacebookTokenService tokenService;
    private final FbPageRepository pageRepository;
    private final FbPageDisconnectLogRepository disconnectLogRepository;

    @Transactional
    public FacebookDisconnectPageResponse disconnect(
            String pageId,
            FacebookDisconnectPageRequest request,
            AuthorizedUser owner
    ) throws CustomException {
        FbPage page = pageAccessGuard.requireConnectedPage(pageId);
        FbPage.TokenStatus previousStatus = page.getTokenStatus();
        LocalDateTime now = LocalDateTime.now();

        page.setConnectionStatus(FbPage.ConnectionStatus.DISCONNECTED);
        page.setTokenStatus(FbPage.TokenStatus.DISCONNECTED);
        page.setDisconnectedAt(now);

        disconnectLogRepository.save(FbPageDisconnectLog.builder()
                .pageId(page.getId())
                .disconnectedBy(owner == null ? null : owner.getId())
                .reason(request == null ? null : request.reason())
                .previousTokenStatus(previousStatus)
                .newTokenStatus(FbPage.TokenStatus.DISCONNECTED)
                .tokenDisabled(true)
                .dataHiddenFromUi(true)
                .disconnectedAt(now)
                .build());
        FbPage savedPage = pageRepository.save(page);
        tokenService.evictPageToken(savedPage.getFbPageId());

        return FacebookDisconnectPageResponse.from(savedPage);
    }
}
