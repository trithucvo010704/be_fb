package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookMessengerProfileResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookMessengerProfileGateway;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FacebookMessengerProfileServiceTest {

    private final FacebookMessengerProfileGateway profileGateway = mock(FacebookMessengerProfileGateway.class);
    private final FacebookTokenService tokenService = mock(FacebookTokenService.class);
    private final FacebookMessengerProfileService service = new FacebookMessengerProfileService(profileGateway, tokenService);

    @Test
    void enrichIfMissingStoresSenderNameAvatarAndRawProfile() {
        FbConversation conversation = FbConversation.builder()
                .senderPsid("psid-1")
                .build();
        when(tokenService.getPageAccessToken("page-1")).thenReturn("page-token");
        when(profileGateway.getProfile("psid-1", "first_name,last_name,name,profile_pic", "page-token"))
                .thenReturn(new FacebookMessengerProfileResponse(
                        "psid-1",
                        "Van",
                        "Nguyen",
                        null,
                        "https://example.com/avatar.jpg"
                ));

        service.enrichIfMissing(conversation, "page-1");

        assertEquals("Van Nguyen", conversation.getSenderName());
        assertEquals("https://example.com/avatar.jpg", conversation.getSenderAvatarUrl());
        assertEquals("psid-1", conversation.getRawProfile().get("id"));
    }

    @Test
    void enrichIfMissingDoesNotCallGraphWhenProfileAlreadyExists() {
        FbConversation conversation = FbConversation.builder()
                .senderPsid("psid-1")
                .senderName("Existing")
                .senderAvatarUrl("https://example.com/existing.jpg")
                .build();

        service.enrichIfMissing(conversation, "page-1");

        verifyNoInteractions(tokenService, profileGateway);
    }
}

