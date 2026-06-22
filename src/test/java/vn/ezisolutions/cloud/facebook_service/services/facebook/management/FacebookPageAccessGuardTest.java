package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FacebookPageAccessGuardTest {

    private final FbPageRepository pageRepository = mock(FbPageRepository.class);
    private final FacebookPageAccessGuard guard = new FacebookPageAccessGuard(pageRepository);

    @Test
    void requireConnectedPageReturnsActiveConnectedPage() throws Exception {
        FbPage page = page(FbPage.ConnectionStatus.CONNECTED, FbPage.TokenStatus.ACTIVE);
        when(pageRepository.findByFbPageId("123")).thenReturn(Optional.of(page));

        FbPage result = guard.requireConnectedPage("123");

        assertSame(page, result);
    }

    @Test
    void requireConnectedPageRejectsDisconnectedPage() {
        FbPage page = page(FbPage.ConnectionStatus.DISCONNECTED, FbPage.TokenStatus.ACTIVE);
        when(pageRepository.findByFbPageId("123")).thenReturn(Optional.of(page));

        CustomException ex = assertThrows(CustomException.class, () -> guard.requireConnectedPage("123"));

        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void requireConnectedPageRejectsInactiveToken() {
        FbPage page = page(FbPage.ConnectionStatus.CONNECTED, FbPage.TokenStatus.REVOKED);
        when(pageRepository.findByFbPageId("123")).thenReturn(Optional.of(page));

        CustomException ex = assertThrows(CustomException.class, () -> guard.requireConnectedPage("123"));

        assertEquals(400, ex.getStatusCode());
    }

    @Test
    void requireConnectedPageSupportsUuidLookup() throws Exception {
        UUID id = UUID.randomUUID();
        FbPage page = page(FbPage.ConnectionStatus.CONNECTED, FbPage.TokenStatus.ACTIVE);
        when(pageRepository.findById(id)).thenReturn(Optional.of(page));

        FbPage result = guard.requireConnectedPage(id.toString());

        assertSame(page, result);
    }

    private FbPage page(FbPage.ConnectionStatus connectionStatus, FbPage.TokenStatus tokenStatus) {
        return FbPage.builder()
                .fbPageId("123")
                .pageName("Page")
                .connectionStatus(connectionStatus)
                .tokenStatus(tokenStatus)
                .build();
    }
}
