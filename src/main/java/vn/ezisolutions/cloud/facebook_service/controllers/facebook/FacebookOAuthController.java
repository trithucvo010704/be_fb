package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthCallbackResponse;
import vn.ezisolutions.cloud.facebook_service.services.facebook.auth.FacebookOAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/facebook/oauth")
public class FacebookOAuthController {
    private final FacebookOAuthService oauthService;

    @GetMapping("/url")
    public BaseResponse url() {
        return BaseResponse.success(oauthService.buildAuthorizationUrl());
    }

    @GetMapping("/callback")
    public BaseResponse callback(
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription) {
        return BaseResponse.success(new FacebookOAuthCallbackResponse(code, state, error, errorDescription));
    }
}
