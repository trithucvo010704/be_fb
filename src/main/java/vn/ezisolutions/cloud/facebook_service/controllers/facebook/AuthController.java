package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.utils.SecurityUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.DemoLoginRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.AuthorizedUserResponse;
import vn.ezisolutions.cloud.facebook_service.services.security.DemoAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final DemoAuthService demoAuthService;

    @PostMapping("/demo-login")
    public BaseResponse demoLogin(@Valid @RequestBody DemoLoginRequest request) throws CustomException {
        return BaseResponse.success("LOGIN_SUCCESS", demoAuthService.login(request.getEmail(), request.getPassword()));
    }

    @GetMapping("/me")
    public BaseResponse me() throws CustomException {
        AuthorizedUser user = SecurityUtils.getCurrentAuthorizedUser()
                .orElseThrow(() -> new CustomException(401, "Unauthenticated"));
        return BaseResponse.success(AuthorizedUserResponse.from(user));
    }

    @PostMapping("/logout")
    public BaseResponse logout() {
        return BaseResponse.success("LOGOUT_SUCCESS");
    }
}
