package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookConnectedPageResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPageInfoResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPagesDataResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageClient;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookPageGateway;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookUserGateway;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageClientRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacebookPageConnectService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookPageConnectService.class);

    private final FacebookUserGateway userGateway;
    private final FacebookPageGateway pageGateway;
    private final FbUserRepository userRepository;
    private final FbPageRepository pageRepository;
    private final FbPageClientRepository pageClientRepository;
    @Qualifier("facebookRestTemplate")
    private final RestTemplate facebookRestTemplate;

    @Transactional
    public List<FacebookConnectedPageResponse> connect(String userAccessToken, AuthorizedUser owner) throws CustomException {
        if (owner == null) {
            throw new CustomException(401, "Unauthenticated");
        }
        var profile = userGateway.getMe("id,name", userAccessToken);
        if (profile == null || profile.id() == null) {
            throw new CustomException(400, "Không lấy được thông tin Facebook user");
        }
        logger.info("Facebook connect started for fbUserId={}, ownerUserId={}", profile.id(), owner.getId());
        LocalDateTime now = LocalDateTime.now();
        FbUser fbUser = userRepository.findByFbUserId(profile.id())
                .orElseGet(FbUser::new);
        fbUser.setOwnerUserId(owner.getId());
        fbUser.setFbUserId(profile.id());
        fbUser.setName(profile.name() == null ? profile.id() : profile.name());
        fbUser.setAccessToken(userAccessToken);
        fbUser.setTokenStatus(FbUser.TokenStatus.ACTIVE);
        FbUser savedUser = userRepository.save(fbUser);

        List<FbPageInfoResponse> pages = fetchAllUserPages(userAccessToken);
        logger.info("Facebook /me/accounts returned {} page(s) for fbUserId={}, ownerUserId={}",
                pages.size(),
                profile.id(),
                owner.getId()
        );

        List<FacebookConnectedPageResponse> result = new ArrayList<>();
        for (FbPageInfoResponse pageInfo : pages) {
            if (pageInfo.id() == null || pageInfo.id().isBlank()) {
                logger.warn("Skipping Facebook page without id for fbUserId={}, ownerUserId={}", profile.id(), owner.getId());
                continue;
            }
            logger.info("Connecting Facebook page id={}, name={}, ownerUserId={}", pageInfo.id(), pageInfo.name(), owner.getId());
            FbPage page = upsertPage(pageInfo, savedUser, owner, now);
            upsertPageClient(page, savedUser, owner, now);
            result.add(toResponse(page));
        }
        return result;
    }

    private List<FbPageInfoResponse> fetchAllUserPages(String userAccessToken) {
        List<FbPageInfoResponse> pages = new ArrayList<>();
        String authHeader = toBearer(userAccessToken);
        FbPagesDataResponse response = pageGateway.getUserPages(
                authHeader,
                FacebookConstants.PAGE_LIST_FIELDS,
                100
        );

        while (response != null) {
            if (response.data() != null) {
                pages.addAll(response.data());
            }
            String next = response.paging() == null ? null : response.paging().next();
            if (next == null || next.isBlank()) {
                break;
            }
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, authHeader);
            response = facebookRestTemplate.exchange(
                    next,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    FbPagesDataResponse.class
            ).getBody();
        }
        return pages;
    }

    private FbPage upsertPage(FbPageInfoResponse pageInfo, FbUser user, AuthorizedUser owner, LocalDateTime now) {
        FbPage page = pageRepository.findByFbPageId(pageInfo.id()).orElseGet(FbPage::new);
        page.setFbPageId(pageInfo.id());
        page.setPageName(pageInfo.name());
        page.setCategory(pageInfo.category());
        page.setConnectedByFbUserId(user.getId());
        page.setConnectedByUserId(owner.getId());
        page.setPageAccessToken(pageInfo.accessToken());
        page.setPagePermissions(pageInfo.tasks() == null ? List.of() : new ArrayList<>(pageInfo.tasks()));
        page.setTokenStatus(FbPage.TokenStatus.ACTIVE);
        page.setConnectionStatus(FbPage.ConnectionStatus.CONNECTED);
        page.setLastSyncedAt(now);
        if (page.getConnectedAt() == null) {
            page.setConnectedAt(now);
        }
        subscribeWebhook(pageInfo, page, now);
        return pageRepository.save(page);
    }

    private void subscribeWebhook(FbPageInfoResponse pageInfo, FbPage page, LocalDateTime now) {
        try {
            pageGateway.subscribePageWebhook(
                    toBearer(pageInfo.accessToken()),
                    pageInfo.id(),
                    FacebookConstants.WEBHOOK_SUBSCRIBE_FIELDS
            );
            page.setWebhookSubscribed(true);
            page.setWebhookSubscribedAt(now);
        } catch (Exception ignored) {
            page.setWebhookSubscribed(false);
        }
    }

    private void upsertPageClient(FbPage page, FbUser user, AuthorizedUser owner, LocalDateTime now) {
        FbPageClient pageClient = pageClientRepository.findByPageIdAndClientId(page.getId(), "central_review")
                .orElseGet(FbPageClient::new);
        pageClient.setPageId(page.getId());
        pageClient.setClientId("central_review");
        pageClient.setConnectedByFbUserId(user.getId());
        pageClient.setConnectedByUserId(owner.getId());
        pageClient.setStatus(FbPageClient.Status.CONNECTED);
        pageClient.setMessageEnabled(true);
        pageClient.setPostEnabled(true);
        if (pageClient.getConnectedAt() == null) {
            pageClient.setConnectedAt(now);
        }
        pageClientRepository.save(pageClient);
    }

    private FacebookConnectedPageResponse toResponse(FbPage page) {
        return new FacebookConnectedPageResponse(
                page.getId(),
                page.getFbPageId(),
                page.getPageName(),
                page.getCategory(),
                page.getTokenStatus().name(),
                page.getConnectionStatus().name(),
                page.getWebhookSubscribed(),
                page.getGrantedPermissions(),
                page.getMissingPermissions()
        );
    }

    private String toBearer(String token) {
        if (token == null) {
            return "";
        }
        return token.toLowerCase().startsWith("bearer ")
                ? token.trim()
                : "Bearer " + token.trim();
    }
}
