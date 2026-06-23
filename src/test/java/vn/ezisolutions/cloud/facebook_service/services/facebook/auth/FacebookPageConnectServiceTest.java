package vn.ezisolutions.cloud.facebook_service.services.facebook.auth;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPageInfoResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbPagesDataResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbUserProfileResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageClient;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookPageGateway;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookUserGateway;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageClientRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FacebookPageConnectServiceTest {

    private final FacebookUserGateway userGateway = mock(FacebookUserGateway.class);
    private final FacebookPageGateway pageGateway = mock(FacebookPageGateway.class);
    private final FbUserRepository userRepository = mock(FbUserRepository.class);
    private final FbPageRepository pageRepository = mock(FbPageRepository.class);
    private final FbPageClientRepository pageClientRepository = mock(FbPageClientRepository.class);
    private final RestTemplate facebookRestTemplate = mock(RestTemplate.class);
    private final FacebookPageConnectService service = new FacebookPageConnectService(
            userGateway,
            pageGateway,
            userRepository,
            pageRepository,
            pageClientRepository,
            facebookRestTemplate
    );

    @Test
    void connectSavesUserPagesAndPageClientWithPlainTokens() throws Exception {
        AuthorizedUser owner = AuthorizedUser.builder().id("demo-user").name("Demo").username("demo").build();
        when(userGateway.getMe("id,name", "user-token"))
                .thenReturn(new FbUserProfileResponse("fb-user-1", "Facebook User"));
        when(userRepository.findByFbUserId("fb-user-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(FbUser.class))).thenAnswer(invocation -> {
            FbUser user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(pageGateway.getUserPages(eq("Bearer user-token"), anyString(), eq(100)))
                .thenReturn(new FbPagesDataResponse(List.of(
                        new FbPageInfoResponse("page-1", "Page One", "page-token", "Community", List.of("MESSAGING"))
                ), null));
        when(pageRepository.findByFbPageId("page-1")).thenReturn(Optional.empty());
        when(pageRepository.save(any(FbPage.class))).thenAnswer(invocation -> {
            FbPage page = invocation.getArgument(0);
            page.setId(UUID.randomUUID());
            return page;
        });
        when(pageClientRepository.findByPageIdAndClientId(any(UUID.class), eq("central_review")))
                .thenReturn(Optional.empty());
        when(pageClientRepository.save(any(FbPageClient.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.connect("user-token", owner);

        assertEquals(1, result.size());
        assertEquals("page-1", result.get(0).fbPageId());
        ArgumentCaptor<FbUser> userCaptor = ArgumentCaptor.forClass(FbUser.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("fb-user-1", userCaptor.getValue().getFbUserId());
        assertEquals("user-token", userCaptor.getValue().getAccessToken());

        ArgumentCaptor<FbPage> pageCaptor = ArgumentCaptor.forClass(FbPage.class);
        verify(pageRepository).save(pageCaptor.capture());
        assertEquals("page-1", pageCaptor.getValue().getFbPageId());
        assertEquals(FbPage.ConnectionStatus.CONNECTED, pageCaptor.getValue().getConnectionStatus());
        assertEquals("page-token", pageCaptor.getValue().getPageAccessToken());

        verify(pageGateway).subscribePageWebhook(eq("Bearer page-token"), eq("page-1"), anyString());
        verify(pageClientRepository).save(any(FbPageClient.class));
    }
}
