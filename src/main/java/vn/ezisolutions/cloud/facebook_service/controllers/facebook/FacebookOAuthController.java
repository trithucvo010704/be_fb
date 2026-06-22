package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.services.cache.AuthCacheService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.auth.FacebookOAuthService;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facebook/oauth")
public class FacebookOAuthController {
    private final FacebookOAuthService oauthService;
    private final AuthCacheService authCacheService;

    @GetMapping("/url")
    public BaseResponse url(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return BaseResponse.success(oauthService.buildAuthorizationUrl(resolveAuthorizedUser(authorization).orElse(null)));
    }

    @GetMapping("/callback")
    public BaseResponse callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) throws CustomException {
        return BaseResponse.success(oauthService.handleCallback(code, state, error, errorDescription));
    }

    private Optional<AuthorizedUser> resolveAuthorizedUser(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(authCacheService.getUserByToken(authorization.substring("Bearer ".length())));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
