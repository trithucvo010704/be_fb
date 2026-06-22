package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.utils.SecurityUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPageConnectRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookConnectedPageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.auth.FacebookPageConnectService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;

@RestController
@RequiredArgsConstructor
public class FacebookPageController {
    private final FacebookPageConnectService pageConnectService;
    private final FacebookPageAccessGuard pageAccessGuard;
    private final FbPageRepository pageRepository;

    @PostMapping("/api/facebook/pages/connect")
    public BaseResponse connect(@Valid @RequestBody FacebookPageConnectRequest request) throws CustomException {
        AuthorizedUser owner = SecurityUtils.getCurrentAuthorizedUser()
                .orElseThrow(() -> new CustomException(401, "Unauthenticated"));
        return BaseResponse.success("PAGES_CONNECTED", pageConnectService.connect(request.getUserAccessToken(), owner));
    }

    @GetMapping("/api/pages")
    public BaseResponse pages() {
        return BaseResponse.success(pageRepository.findByConnectionStatusOrderByUpdatedAtDesc(FbPage.ConnectionStatus.CONNECTED)
                .stream()
                .map(this::toResponse)
                .toList());
    }

    @GetMapping("/api/pages/{pageId}")
    public BaseResponse pageDetail(@PathVariable String pageId) throws CustomException {
        return BaseResponse.success(toResponse(pageAccessGuard.requireConnectedPage(pageId)));
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
}
