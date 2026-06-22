package vn.ezisolutions.cloud.facebook_service.services.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.shared.RoleDef;
import vn.ezisolutions.cloud.facebook_service.dto.response.DemoLoginResponse;
import vn.ezisolutions.cloud.facebook_service.entity.id_system.BizUserEntity;
import vn.ezisolutions.cloud.facebook_service.services.cache.AuthCacheService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DemoAuthService {
    private final AuthCacheService authCacheService;

    @Value("${app.demo.email}")
    private String demoEmail;

    @Value("${app.demo.password}")
    private String demoPassword;

    @Value("${app.demo.user-id}")
    private String demoUserId;

    @Value("${app.demo.name}")
    private String demoName;

    public DemoLoginResponse login(String email, String password) throws CustomException {
        if (!demoEmail.equalsIgnoreCase(email) || !demoPassword.equals(password)) {
            throw new CustomException(401, "Demo account không hợp lệ");
        }
        String token = UUID.randomUUID().toString();
        BizUserEntity demoUser = BizUserEntity.builder()
                .id(demoUserId)
                .email(demoEmail)
                .name(demoName)
                .build();
        try {
            authCacheService.cacheAuth(token, demoUser, Instant.now().plus(8, ChronoUnit.HOURS));
        } catch (JsonProcessingException e) {
            throw new CustomException(500, "Không thể tạo phiên đăng nhập demo", e);
        }
        AuthorizedUser authorizedUser = AuthorizedUser.builder()
                .id(demoUserId)
                .name(demoName)
                .username(demoEmail)
                .roles(List.of(RoleDef.USER))
                .build();
        return new DemoLoginResponse(token, authorizedUser);
    }
}
