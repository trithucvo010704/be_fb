package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;

public record DemoLoginResponse(
        String token,
        AuthorizedUser user
) {
}
