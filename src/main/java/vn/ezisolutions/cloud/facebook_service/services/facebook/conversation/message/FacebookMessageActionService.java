package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.FacebookErrorCategory;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbMessageEvent;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

@Service
@RequiredArgsConstructor
public class FacebookMessageActionService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookMessageActionService.class);

    private final FacebookTokenService facebookTokenService;
    private final FacebookMessageClientService facebookMessageService;

    public void processOutboundMessage(FbMessageEvent payload) throws CustomException {
        logger.info("[MESSAGE-ACTION] Start processOutboundMessage - pageId: {}, recipientId: {}", payload.getPageId(),
                payload.getRecipientId());

        String pageAccessToken = facebookTokenService.getPageAccessToken(payload.getPageId());
        if (pageAccessToken == null) {
            throw new CustomException(404, "Token not found for PageID: " + payload.getPageId());
        }

        String recipientId = payload.getRecipientId();
        String content = payload.getContent();
        MessageType type = payload.getType() != null ? payload.getType() : MessageType.TEXT;

        logger.info("[MESSAGE-ACTION] Sending outbound message of type: {} to recipient: {}", type, recipientId);

        try {
            if (type == MessageType.TEXT) {
                facebookMessageService.sendTextMessage(pageAccessToken, recipientId, content);
            } else if (type.isAttachment()) {
                facebookMessageService.sendAttachmentMessage(pageAccessToken, recipientId, type.toValue(), content);
            } else {
                logger.warn("[MESSAGE-ACTION] Unsupported message type: '{}'. Fallback to text.", type);
                facebookMessageService.sendTextMessage(pageAccessToken, recipientId,
                        "Unsupported format: " + content);
            }
        } catch (FacebookApiException e) {
            handleFacebookError(e);
        }

        logger.info("[MESSAGE-ACTION] processOutboundMessage success - pageId: {}, recipientId: {}",
                payload.getPageId(), payload.getRecipientId());
    }

    private void handleFacebookError(FacebookApiException e) throws CustomException {
        int code = e.getFbErrorCode();
        int subcode = e.getFbErrorSubcode();
        String userFriendlyMessage = e.getUserFriendlyMessage();
        logger.error("[MESSAGE-ACTION] FB API Error: code={}, subcode={}, message={}", code, subcode,
                e.getMessage());

        if (code == 190 || code == 102 || code == 10 || code == 200) {
            throw new CustomException(400,
                    "Mất kết nối Fanpage hoặc phiên đăng nhập hết hạn: " + userFriendlyMessage);
        } else if (code == -1 || e.getCategory() == FacebookErrorCategory.RATE_LIMIT
                || e.getCategory() == FacebookErrorCategory.SYSTEM) {
            throw new CustomException(500, "Lỗi mạng hoặc giới hạn API từ Facebook: " + e.getMessage());
        } else {
            throw new CustomException(400, "Lỗi nghiệp vụ gửi tin nhắn Facebook: " + userFriendlyMessage);
        }
    }
}
