package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPageInfoResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookPageGateway;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FacebookPermissionService {
    private static final Logger log = LoggerFactory.getLogger(FacebookPermissionService.class);

    private final FbUserRepository fbUserRepository;
    private final FbPageRepository fbPageRepository;
    private final FacebookPageGateway pageClient;

    private String toBearer(String token) {
        if (token == null) return "";
        if (token.toLowerCase().startsWith("bearer ")) {
            return token.trim();
        }
        return "Bearer " + token.trim();
    }

    private void syncUserPages(String accessToken, FbUser owner) {
        var response = pageClient.getUserPages(
                toBearer(accessToken),
                FacebookConstants.PAGE_LIST_FIELDS,
                100
        );
        List<FbPageInfoResponse> pages = response != null ? response.data() : List.of();
        int pageCount = pages.size();

        if (pageCount == 0) {
            log.warn("[LOGIN-SYNC-PAGES] User {} không có page hoặc API trả rỗng", owner.getFbUserId());
            return;
        }

        log.info("[LOGIN-SYNC-PAGES] Facebook trả {} page(s) cho fbUserId={}", pageCount, owner.getFbUserId());

        LocalDateTime now = LocalDateTime.now();

        List<String> pageIds = pages.stream()
                .map(FbPageInfoResponse::id)
                .toList();

        Map<String, FbPage> existingPages = fbPageRepository
                .findByFbPageIdIn(pageIds)
                .stream()
                .collect(Collectors.toMap(FbPage::getFbPageId, p -> p));

        List<FbPage> pagesToSave = pages.stream()
                .map(p -> processSinglePage(p, existingPages, owner, now))
                .toList();

        fbPageRepository.saveAll(pagesToSave);

        log.info("[LOGIN-SYNC-PAGES] Sync thành công {} page(s) cho fbUserId={}", pagesToSave.size(), owner.getFbUserId());
    }

    private FbPage processSinglePage(FbPageInfoResponse p, Map<String, FbPage> existingPages, FbUser owner, LocalDateTime now) {
        FbPage page = existingPages.get(p.id());

        if (page == null) {
            page = FbPage.builder()
                    .fbPageId(p.id())
                    .connectedAt(now)
                    .createdAt(now)
                    .build();
        }

        page.setPageName(p.name());
        page.setCategory(p.category());
        page.setPageAccessToken(p.accessToken());
        page.setConnectedByFbUserId(owner.getId());
        page.setConnectedByUserId(owner.getOwnerId());
        page.setPagePermissions(p.tasks() != null ? new ArrayList<>(p.tasks()) : List.of());
        page.setTokenStatus(FbPage.TokenStatus.ACTIVE);
        page.setConnectionStatus(FbPage.ConnectionStatus.CONNECTED);
        page.setLastSyncedAt(now);
        page.setUpdatedAt(now);

        if (!Boolean.TRUE.equals(page.getWebhookSubscribed())) {
            subscribeWebhookWithCatch(p, page);
        }

        return page;
    }

    private void subscribeWebhookWithCatch(FbPageInfoResponse p, FbPage page) {
        try {
            pageClient.subscribePageWebhook(
                    toBearer(p.accessToken()),
                    p.id(),
                    FacebookConstants.WEBHOOK_SUBSCRIBE_FIELDS
            );
            page.setWebhookSubscribed(true);
            page.setWebhookSubscribedAt(LocalDateTime.now());
            log.info("[WEBHOOK] Subscribed webhook for page: {}", p.name());
        } catch (FacebookApiException e) {
            page.setWebhookSubscribed(false);
            log.error("[WEBHOOK] Facebook API Error đăng ký webhook - pageId: {}, errorCode: {}, error: {}",
                    p.id(), e.getFbErrorCode(), e.getMessage(), e);
        } catch (Exception e) {
            page.setWebhookSubscribed(false);
            log.error("[WEBHOOK] Lỗi hệ thống khi đăng ký webhook - pageId: {}, error: {}",
                    p.id(), e.getMessage(), e);
        }
    }

    public void processSyncAfterLogin(String ownerId, String fbUserId) throws CustomException {
        log.info("Start processSyncAfterLogin - ownerId: {}, fbUserId: {}", ownerId, fbUserId);
        FbUser user = fbUserRepository.findByFbUserId(fbUserId)
                .orElseThrow(() -> new CustomException(404, "Không tìm thấy user Facebook " + fbUserId + " trong database."));

        try {
            syncUserPages(user.getAccessToken(), user);
            log.info("processSyncAfterLogin Pages sync success - fbUserId: {}", fbUserId);
        } catch (Exception e) {
            log.error("[SYNC-AFTER-LOGIN] Thất bại khi đồng bộ Pages: {}", e.getMessage(), e);
        }
    }
}
