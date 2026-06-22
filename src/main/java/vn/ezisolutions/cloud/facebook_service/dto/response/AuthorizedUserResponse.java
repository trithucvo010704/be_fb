package vn.ezisolutions.cloud.facebook_service.dto.response;

import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;

import java.util.List;

public record AuthorizedUserResponse(
        String id,
        String name,
        String username,
        List<String> roles
) {
    public static AuthorizedUserResponse from(AuthorizedUser user) {
        return new AuthorizedUserResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getRoles()
        );
    }
}

