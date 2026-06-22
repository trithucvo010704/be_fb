package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacebookMessengerInboxServiceTest {

    private final FacebookPageAccessGuard pageAccessGuard = mock(FacebookPageAccessGuard.class);
    private final FbConversationRepository conversationRepository = mock(FbConversationRepository.class);
    private final FbMessageRepository messageRepository = mock(FbMessageRepository.class);
    private final FacebookMessengerInboxService service = new FacebookMessengerInboxService(
            pageAccessGuard,
            conversationRepository,
            messageRepository
    );

    @Test
    void listConversationsReturnsOnlyConversationsForConnectedPage() throws Exception {
        UUID pageId = UUID.randomUUID();
        FbPage page = FbPage.builder().id(pageId).fbPageId("page-1").pageName("Page").build();
        FbConversation conversation = FbConversation.builder()
                .id(UUID.randomUUID())
                .pageId(pageId)
                .senderPsid("psid-1")
                .unreadCount(2)
                .build();
        when(pageAccessGuard.requireConnectedPage("page-1")).thenReturn(page);
        when(conversationRepository.findByPageIdOrderByLastMessageAtDesc(eq(pageId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(conversation)));

        var result = service.listConversations("page-1", 1, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals("psid-1", result.getContent().getFirst().senderPsid());
        verify(messageRepository, never()).findAll();
    }
}

