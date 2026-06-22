package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbCommentHideResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbCommentResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookCommentGateway;
import vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message.FacebookMessageClientService;

@Service
@RequiredArgsConstructor
public class FacebookCommentClientService {
    private final FacebookMessageClientService facebookMessageService;
    private final FacebookCommentGateway commentClient;

    public FbMessageResponse sendInboxMessage(String fbCommentId, String messageContent, String pageToken) throws FacebookApiException {
        FbSendMessageRequest requestBody = FbSendMessageRequest.ofCommentReply(fbCommentId, messageContent);
        return facebookMessageService.sendMessage(pageToken, requestBody);
    }

    public FbCommentResponse replyToComment(String fbCommentId, String messageContent, String pageToken) throws FacebookApiException {
        try {
            return commentClient.replyToComment(fbCommentId, messageContent, pageToken);
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi gọi Facebook API: " + e.getMessage(), null);
        }
    }

    public FbCommentHideResponse hideComment(String fbCommentId, String pageToken) throws FacebookApiException {
        try {
            return commentClient.hideComment(fbCommentId, true, pageToken);
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi gọi Facebook API: " + e.getMessage(), null);
        }
    }
}
