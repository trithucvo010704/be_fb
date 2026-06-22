package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDisconnectPageRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageDisconnectLog;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageDisconnectLogRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacebookPageDisconnectServiceTest {

    private final FacebookPageAccessGuard pageAccessGuard = mock(FacebookPageAccessGuard.class);
    private final FacebookTokenService tokenService = mock(FacebookTokenService.class);
    private final FbPageRepository pageRepository = mock(FbPageRepository.class);
    private final FbPageDisconnectLogRepository disconnectLogRepository = mock(FbPageDisconnectLogRepository.class);
    private final FacebookPageDisconnectService service = new FacebookPageDisconnectService(
            pageAccessGuard,
            tokenService,
            pageRepository,
            disconnectLogRepository
    );

    @Test
    void disconnectMarksPageAndWritesLog() throws Exception {
        FbPage page = FbPage.builder()
                .id(UUID.randomUUID())
                .fbPageId("page-1")
                .pageName("Page")
                .connectionStatus(FbPage.ConnectionStatus.CONNECTED)
                .tokenStatus(FbPage.TokenStatus.ACTIVE)
                .build();
        when(pageAccessGuard.requireConnectedPage("page-1")).thenReturn(page);
        when(pageRepository.save(any(FbPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.disconnect(
                "page-1",
                new FacebookDisconnectPageRequest("review done"),
                AuthorizedUser.builder().id("demo").build()
        );

        assertEquals(FbPage.ConnectionStatus.DISCONNECTED, response.connectionStatus());
        assertEquals(FbPage.TokenStatus.DISCONNECTED, response.tokenStatus());
        verify(tokenService).evictPageToken("page-1");
        ArgumentCaptor<FbPageDisconnectLog> logCaptor = ArgumentCaptor.forClass(FbPageDisconnectLog.class);
        verify(disconnectLogRepository).save(logCaptor.capture());
        assertEquals("demo", logCaptor.getValue().getDisconnectedBy());
    }
}

