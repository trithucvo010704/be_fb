package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FacebookMessengerServiceTest {

    private final FacebookPageAccessGuard pageAccessGuard = mock(FacebookPageAccessGuard.class);
    private final FacebookTokenService tokenService = mock(FacebookTokenService.class);
    private final FacebookMessageClientService messageClientService = mock(FacebookMessageClientService.class);
    private final FbConversationRepository conversationRepository = mock(FbConversationRepository.class);
    private final FbMessageRepository messageRepository = mock(FbMessageRepository.class);
    private final FacebookMessengerService service = new FacebookMessengerService(
            pageAccessGuard,
            tokenService,
            messageClientService,
            conversationRepository,
            messageRepository
    );

    @Test
    void sendMessageSendsTextAndPersistsOutboundMessage() throws Exception {
        FbPage page = page();
        when(pageAccessGuard.requireConnectedPage("page-1")).thenReturn(page);
        when(tokenService.getPageAccessToken("page-1")).thenReturn("page-token");
        when(conversationRepository.findByPageIdAndSenderPsid(page.getId(), "psid-1")).thenReturn(Optional.empty());
        when(conversationRepository.save(any(FbConversation.class))).thenAnswer(invocation -> {
            FbConversation conversation = invocation.getArgument(0);
            conversation.setId(UUID.randomUUID());
            return conversation;
        });
        when(messageClientService.sendMessage(eq("page-token"), any()))
                .thenReturn(new FbMessageResponse("psid-1", "mid-1"));
        when(messageRepository.save(any(FbMessage.class))).thenAnswer(invocation -> {
            FbMessage message = invocation.getArgument(0);
            message.setId(UUID.randomUUID());
            return message;
        });

        var response = service.sendMessage(
                "page-1",
                new FacebookSendMessageRequest("psid-1", "Xin chào", FbMessage.MessageType.TEXT),
                AuthorizedUser.builder().id("demo").build()
        );

        assertEquals("mid-1", response.facebookMessageId());
        assertEquals(FbMessage.Status.SENT, response.status());
        ArgumentCaptor<FbMessage> messageCaptor = ArgumentCaptor.forClass(FbMessage.class);
        verify(messageRepository).save(messageCaptor.capture());
        assertEquals(FbMessage.Direction.OUTBOUND, messageCaptor.getValue().getDirection());
        assertEquals("Xin chào", messageCaptor.getValue().getContent());
    }

    private FbPage page() {
        return FbPage.builder()
                .id(UUID.randomUUID())
                .fbPageId("page-1")
                .pageName("Page")
                .connectionStatus(FbPage.ConnectionStatus.CONNECTED)
                .tokenStatus(FbPage.TokenStatus.ACTIVE)
                .build();
    }
}

