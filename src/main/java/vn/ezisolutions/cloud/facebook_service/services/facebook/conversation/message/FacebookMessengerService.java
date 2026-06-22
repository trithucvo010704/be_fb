package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookMessageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbConversationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbMessageRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FacebookMessengerService {

    private final FacebookPageAccessGuard pageAccessGuard;
    private final FacebookTokenService tokenService;
    private final FacebookMessageClientService messageClientService;
    private final FbConversationRepository conversationRepository;
    private final FbMessageRepository messageRepository;

    @Transactional
    public FacebookMessageResponse sendMessage(
            String pageId,
            FacebookSendMessageRequest request,
            AuthorizedUser owner
    ) throws CustomException {
        FbPage page = pageAccessGuard.requireConnectedPage(pageId);
        String token = tokenService.getPageAccessToken(page.getFbPageId());
        if (token == null || token.isBlank()) {
            throw new CustomException(400, "Page token không khả dụng hoặc đã hết hạn");
        }

        LocalDateTime now = LocalDateTime.now();
        FbConversation conversation = conversationRepository.findByPageIdAndSenderPsid(page.getId(), request.recipientPsid())
                .orElseGet(() -> FbConversation.builder()
                        .pageId(page.getId())
                        .senderPsid(request.recipientPsid())
                        .status(FbConversation.Status.OPEN)
                        .unreadCount(0)
                        .build());
        conversation.setLastMessageAt(now);
        conversation.setLastOutboundAt(now);
        FbConversation savedConversation = conversationRepository.save(conversation);

        FbMessage message = FbMessage.builder()
                .conversationId(savedConversation.getId())
                .pageId(page.getId())
                .direction(FbMessage.Direction.OUTBOUND)
                .senderId(page.getFbPageId())
                .recipientId(request.recipientPsid())
                .messageType(request.resolvedMessageType())
                .content(request.resolvedMessageType() == FbMessage.MessageType.TEXT ? request.content() : null)
                .attachmentUrl(request.resolvedMessageType() == FbMessage.MessageType.TEXT ? null : request.content())
                .status(FbMessage.Status.SENT)
                .occurredAt(now)
                .build();

        try {
            vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse graphResponse;
            if (request.resolvedMessageType() == FbMessage.MessageType.TEXT) {
                graphResponse = messageClientService.sendMessage(
                        token,
                        vn.ezisolutions.cloud.facebook_service.dto.request.FbSendMessageRequest.ofText(
                                request.recipientPsid(),
                                request.content()
                        )
                );
            } else if (request.resolvedMessageType() == FbMessage.MessageType.IMAGE) {
                graphResponse = messageClientService.sendMessage(
                        token,
                        vn.ezisolutions.cloud.facebook_service.dto.request.FbSendMessageRequest.ofAttachment(
                                request.recipientPsid(),
                                "image",
                                request.content()
                        )
                );
            } else {
                throw new CustomException(400, "Phase 5 chỉ hỗ trợ gửi TEXT hoặc IMAGE URL");
            }
            message.setFacebookMessageId(graphResponse.messageId());
            message.setStatus(FbMessage.Status.SENT);
        } catch (FacebookApiException e) {
            message.setStatus(FbMessage.Status.FAILED);
            String errorMessage = e.getFbErrorCode() == -1 ? e.getMessage() : e.getUserFriendlyMessage();
            message.setErrorMessage(errorMessage);
            messageRepository.save(message);
            throw new CustomException(e.getHttpStatus(), "Gửi tin nhắn Facebook thất bại: " + errorMessage);
        }

        return FacebookMessageResponse.from(messageRepository.save(message));
    }
}
