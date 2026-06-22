package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookConnectedPageResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookOAuthTokenResponse;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookOAuthGateway;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacebookOAuthServiceTest {

    private final FacebookOAuthGateway oauthGateway = mock(FacebookOAuthGateway.class);
    private final FacebookPageConnectService pageConnectService = mock(FacebookPageConnectService.class);

    @Test
    void buildAuthorizationUrlContainsRequiredScopesAndRedirect() {
        FacebookOAuthService service = new FacebookOAuthService(properties(), oauthGateway, pageConnectService);

        var response = service.buildAuthorizationUrl();

        assertNotNull(response.state());
        assertTrue(response.url().contains("client_id=app-id"));
        assertTrue(response.url().contains("redirect_uri=https://example.com/callback"));
        assertTrue(response.url().contains("pages_manage_metadata"));
        assertTrue(response.url().contains("pages_messaging"));
        assertTrue(response.url().contains("pages_manage_posts"));
    }

    @Test
    void handleCallbackExchangesCodeAndConnectsPagesWhenStateHasOwner() throws Exception {
        FacebookOAuthService service = new FacebookOAuthService(properties(), oauthGateway, pageConnectService);
        AuthorizedUser owner = AuthorizedUser.builder().id("demo").name("Demo").build();
        String state = service.buildAuthorizationUrl(owner).state();
        when(oauthGateway.exchangeCode("app-id", "secret", "https://example.com/callback", "code-1"))
                .thenReturn(new FacebookOAuthTokenResponse("user-token", "bearer", 3600L));
        when(pageConnectService.connect("user-token", owner))
                .thenReturn(List.of(new FacebookConnectedPageResponse(
                        UUID.randomUUID(),
                        "page-1",
                        "Page",
                        "Community",
                        "ACTIVE",
                        "CONNECTED",
                        true,
                        List.of(),
                        List.of()
                )));

        var response = service.handleCallback("code-1", state, null, null);

        assertTrue(response.exchanged());
        assertTrue(response.connected());
        assertEquals(1, response.pages().size());
        verify(pageConnectService).connect("user-token", owner);
    }

    private FacebookProperties properties() {
        FacebookProperties properties = new FacebookProperties();
        properties.setAppId("app-id");
        properties.setAppSecret("secret");
        properties.setGraphVersion("v25.0");
        properties.setGraphHost("https://graph.facebook.com");
        properties.setRedirectUri("https://example.com/callback");
        properties.setVerifyToken("verify");
        return properties;
    }
}
