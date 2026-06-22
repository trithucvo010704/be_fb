package vn.ezisolutions.cloud.facebook_service.core.properties;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "facebook")
@Getter
@Setter
@Validated
public class FacebookProperties {

    @NotBlank
    private String appId;

    @NotBlank
    private String appSecret;

    @NotBlank
    private String graphHost = "https://graph.facebook.com";

    @NotBlank
    private String graphVersion = "v24.0";

    @NotBlank
    private String redirectUri;

    @NotBlank
    private String verifyToken;

    private String appCallbackUri = "eziapp://facebook/callback";

    private String webCallbackUri = "http://localhost:4200/facebook-resource";
}
