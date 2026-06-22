package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse;
import vn.ezisolutions.cloud.facebook_service.enums.MessageType;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookMessageGateway;

@Service
@RequiredArgsConstructor
public class FacebookMessageClientService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookMessageClientService.class);
    private final FacebookMessageGateway messageClient;

    public FbMessageResponse sendMessage(String pageToken, FbSendMessageRequest request) throws FacebookApiException {
        try {
            FbMessageResponse response = messageClient.sendMessage(pageToken, request);
            logger.info("Sent FB message successfully. Recipient: {}", request.recipient());
            return response;
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("System Error sending to FB: {}", e.getMessage(), e);
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi gửi tin nhắn qua Facebook API: " + e.getMessage(), null);
        }
    }

    public void sendTextMessage(String pageToken, String recipientId, String content) throws FacebookApiException {
        if (!StringUtils.hasText(pageToken) || !StringUtils.hasText(recipientId) || !StringUtils.hasText(content)) {
            throw new FacebookApiException(400, 0, "Thông tin gửi tin nhắn không hợp lệ (Thiếu Token, ID hoặc Nội dung)", null);
        }

        FbSendMessageRequest request = FbSendMessageRequest.ofText(recipientId, content);

        sendMessage(pageToken, request);
    }

    public void sendAttachmentMessage(String pageToken, String recipientId, String type, String mediaUrl) throws FacebookApiException {
        if (!StringUtils.hasText(pageToken) || !StringUtils.hasText(recipientId) || !StringUtils.hasText(mediaUrl)) {
            throw new FacebookApiException(400, 0, "Thông tin gửi media không hợp lệ (Thiếu Token, ID hoặc URL)", null);
        }

        MessageType messageType = MessageType.from(type);
        if (!messageType.isAttachment()) {
            throw new FacebookApiException(400, 0, "Loại media không hỗ trợ. Chỉ chấp nhận: image, video, audio, file", null);
        }

        FbSendMessageRequest request = FbSendMessageRequest.ofAttachment(recipientId, messageType.toValue(), mediaUrl);
        sendMessage(pageToken, request);
    }
}
