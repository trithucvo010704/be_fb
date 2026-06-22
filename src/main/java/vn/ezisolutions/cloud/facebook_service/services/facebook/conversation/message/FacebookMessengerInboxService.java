package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookConversationResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookMessageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookMessengerInboxService {

    private final FacebookPageAccessGuard pageAccessGuard;
    private final FbConversationRepository conversationRepository;
    private final FbMessageRepository messageRepository;

    public Page<FacebookConversationResponse> listConversations(String pageId, int page, int limit) throws CustomException {
        FbPage fbPage = pageAccessGuard.requireConnectedPage(pageId);
        return conversationRepository.findByPageIdOrderByLastMessageAtDesc(
                        fbPage.getId(),
                        toPageRequest(page, limit)
                )
                .map(FacebookConversationResponse::from);
    }

    public Page<FacebookMessageResponse> listMessages(
            String pageId,
            UUID conversationId,
            int page,
            int limit
    ) throws CustomException {
        FbPage fbPage = pageAccessGuard.requireConnectedPage(pageId);
        FbConversation conversation = conversationRepository.findById(conversationId)
                .filter(item -> fbPage.getId().equals(item.getPageId()))
                .orElseThrow(() -> new CustomException(404, "Conversation không tồn tại trong Page này"));

        return messageRepository.findByConversationIdAndPageIdOrderByOccurredAtAsc(
                        conversation.getId(),
                        fbPage.getId(),
                        toPageRequest(page, limit)
                )
                .map(FacebookMessageResponse::from);
    }

    private PageRequest toPageRequest(int page, int limit) {
        int safePage = Math.max(page, 1) - 1;
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return PageRequest.of(safePage, safeLimit);
    }
}

