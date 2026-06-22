package vn.ezisolutions.cloud.facebook_service.core.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.entity.id_system.BizUserEntity;

import java.util.Optional;

public class SecurityUtils {
    public static Optional<BizUserEntity> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof BizUserEntity) {
            return Optional.of((BizUserEntity) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static Optional<AuthorizedUser> getCurrentAuthorizedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthorizedUser) {
            return Optional.of((AuthorizedUser) authentication.getPrincipal());
        }
        return Optional.empty();
    }
}
