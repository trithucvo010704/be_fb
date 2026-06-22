package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;

import static org.junit.jupiter.api.Assertions.*;

class FacebookOAuthServiceTest {

    @Test
    void buildAuthorizationUrlContainsRequiredScopesAndRedirect() {
        FacebookProperties properties = new FacebookProperties();
        properties.setAppId("app-id");
        properties.setAppSecret("secret");
        properties.setGraphVersion("v25.0");
        properties.setGraphHost("https://graph.facebook.com");
        properties.setRedirectUri("https://example.com/callback");
        properties.setVerifyToken("verify");
        FacebookOAuthService service = new FacebookOAuthService(properties);

        var response = service.buildAuthorizationUrl();

        assertNotNull(response.state());
        assertTrue(response.url().contains("client_id=app-id"));
        assertTrue(response.url().contains("redirect_uri=https://example.com/callback"));
        assertTrue(response.url().contains("pages_manage_metadata"));
        assertTrue(response.url().contains("pages_messaging"));
        assertTrue(response.url().contains("pages_manage_posts"));
    }
}
