package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbMessageEvent;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacebookMessagePersistenceServiceTest {

    private final FbPageRepository pageRepository = mock(FbPageRepository.class);
    private final FbConversationRepository conversationRepository = mock(FbConversationRepository.class);
    private final FbMessageRepository messageRepository = mock(FbMessageRepository.class);
    private final FacebookMessengerProfileService profileService = mock(FacebookMessengerProfileService.class);
    private final FacebookMessagePersistenceService service = new FacebookMessagePersistenceService(
            pageRepository,
            conversationRepository,
            messageRepository,
            profileService
    );

    @Test
    void saveInboundCreatesConversationEnrichesProfileAndStoresMessage() {
        FbPage page = FbPage.builder()
                .id(UUID.randomUUID())
                .fbPageId("page-1")
                .pageName("Page")
                .build();
        when(messageRepository.findByEventId("mid-1")).thenReturn(Optional.empty());
        when(pageRepository.findByFbPageId("page-1")).thenReturn(Optional.of(page));
        when(conversationRepository.findByPageIdAndSenderPsid(page.getId(), "psid-1")).thenReturn(Optional.empty());
        when(conversationRepository.save(any(FbConversation.class))).thenAnswer(invocation -> {
            FbConversation conversation = invocation.getArgument(0);
            conversation.setId(UUID.randomUUID());
            return conversation;
        });

        service.saveInbound(FbMessageEvent.builder()
                .eventId("mid-1")
                .pageId("page-1")
                .senderId("psid-1")
                .recipientId("page-1")
                .content("hello")
                .type(MessageType.TEXT)
                .timestamp(1710000000000L)
                .build());

        verify(profileService).enrichIfMissing(any(FbConversation.class), eq("page-1"));
        verify(messageRepository).save(any(FbMessage.class));
    }
}

