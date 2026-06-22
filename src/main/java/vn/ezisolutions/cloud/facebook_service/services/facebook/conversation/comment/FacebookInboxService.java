package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.comment;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookErrorPayload;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbMessageResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbComment;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbCommentLog;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbCommentLogRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbCommentRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookInboxService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookInboxService.class);
    private final FbCommentRepository fbCommentRepository;
    private final FbCommentLogRepository fbCommentLogRepository;
    private final FacebookCommentClientService facebookCommentClientService;

    public void execute(String internalId, String fbCommentId, String messageContent, String pageToken) throws CustomException {
        String logPrefix = "[INBOX-" + fbCommentId + "]";

        FbComment comment = fbCommentRepository.findById(UUID.fromString(internalId))
                .orElseThrow(() -> new CustomException(404, "Không tìm thấy Comment trong DB"));

        if (Boolean.TRUE.equals(comment.getIsMessaged())) {
            throw new CustomException(404, "Đã gửi tin nhắn trước đó");
        }

        if (isExpiredComment(comment)) {
            logger.warn("{} SKIPPED: Comment quá hạn 7 ngày.", logPrefix);
            saveLog(comment, FbCommentLog.Status.SKIPPED, Map.of("reason", "Quá hạn 7 ngày (Facebook không cho phép gửi)"));
            return;
        }

        try {
            logger.info("{} Sending Request to FB...", logPrefix);
            FbMessageResponse response = facebookCommentClientService.sendInboxMessage(fbCommentId, messageContent, pageToken);
            logger.info("{} Success.", logPrefix);
            handleSuccess(comment, response);
        } catch (FacebookApiException e) {
            handleFacebookError(comment, logPrefix, e);
        } catch (Exception e) {
            logger.error("{} Unexpected system error: {}", logPrefix, e.getMessage(), e);
            saveLog(comment, FbCommentLog.Status.FAILED, Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
            throw new CustomException(500, "Lỗi hệ thống nội bộ: " + e.getMessage());
        }
    }

    private boolean isExpiredComment(FbComment comment) {
        if (comment.getCreatedTimeFb() == null) {
            return false;
        }
        return ChronoUnit.DAYS.between(comment.getCreatedTimeFb(), LocalDateTime.now()) > 7;
    }

    private void handleSuccess(FbComment comment, FbMessageResponse response) {
        comment.setIsMessaged(true);
        comment.setProcessedAt(LocalDateTime.now());
        if (response != null && response.messageId() != null) {
            comment.setFbReplyId(response.messageId());
        }
        fbCommentRepository.save(comment);
        saveLog(comment, FbCommentLog.Status.SUCCESS, response);
    }

    private void handleFacebookError(FbComment comment, String logPrefix, FacebookApiException e) throws CustomException {
        int code = e.getFbErrorCode();
        int subcode = e.getFbErrorSubcode();
        String userMessage = e.getUserFriendlyMessage();
        logger.warn("{} FB Error: {}", logPrefix, userMessage);

        if (code == 10900 || code == 10903) {
            comment.setIsMessaged(true);
            fbCommentRepository.save(comment);
        }

        FbCommentLog.Status suggestedStatus = (code == 100 && subcode != 33)
                ? FbCommentLog.Status.SKIPPED
                : FbCommentLog.Status.FAILED;

        FacebookErrorPayload cleanPayload = FacebookErrorPayload.from(e);
        saveLog(comment, suggestedStatus, cleanPayload);

        if (code == 190 || code == 102 || code == 10 || code == 200) {
            throw new CustomException(400, userMessage);
        }
    }

    private void saveLog(FbComment comment, FbCommentLog.Status status, Object payload) {
        FbCommentLog logEntry = FbCommentLog.builder()
                .commentId(comment.getId())
                .actionType(FbCommentLog.ActionType.SEND_INBOX)
                .status(status)
                .responsePayload(payload)
                .createdAt(LocalDateTime.now())
                .build();
        fbCommentLogRepository.save(logEntry);
    }
}
