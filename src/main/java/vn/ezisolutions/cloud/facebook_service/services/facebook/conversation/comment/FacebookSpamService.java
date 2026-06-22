package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.comment;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookErrorPayload;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbCommentHideResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbComment;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbCommentLog;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbCommentLogRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbCommentRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookSpamService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookSpamService.class);
    private final FbCommentRepository fbCommentRepository;
    private final FbCommentLogRepository fbCommentLogRepository;
    private final FacebookCommentClientService facebookCommentClientService;

    public void execute(String internalId, String fbCommentId, String pageToken) throws CustomException {
        String logPrefix = "[HIDE-" + fbCommentId + "]";

        if (internalId == null || fbCommentId == null || pageToken == null) {
            throw new CustomException(400, "Missing required parameters (internalId, fbCommentId, or pageToken)");
        }

        FbComment comment = fbCommentRepository.findById(UUID.fromString(internalId))
                .orElseThrow(() -> new CustomException(404, "Comment not found in DB with ID: " + internalId));

        try {
            FbCommentHideResponse response = facebookCommentClientService.hideComment(fbCommentId, pageToken);
            logger.info("{} Success.", logPrefix);

            comment.setIsHidden(true);
            comment.setProcessedAt(LocalDateTime.now());
            fbCommentRepository.save(comment);
            saveLog(comment, FbCommentLog.Status.SUCCESS, response);

        } catch (FacebookApiException e) {
            int code = e.getFbErrorCode();
            int subcode = e.getFbErrorSubcode();
            String userMessage = e.getUserFriendlyMessage();
            logger.warn("{} FB Error: {}", logPrefix, userMessage);

            FbCommentLog.Status suggestedStatus = (code == 100 && subcode != 33)
                    ? FbCommentLog.Status.SKIPPED
                    : FbCommentLog.Status.FAILED;

            FacebookErrorPayload cleanPayload = FacebookErrorPayload.from(e);
            saveLog(comment, suggestedStatus, cleanPayload);
        } catch (Exception e) {
            logger.error("{} System Error: {}", logPrefix, e.getMessage(), e);
            saveLog(comment, FbCommentLog.Status.FAILED, Map.of("error_type", "SYSTEM_ERROR", "message", e.getMessage()));
        }
    }

    private void saveLog(FbComment comment, FbCommentLog.Status status, Object payload) {
        FbCommentLog logEntry = FbCommentLog.builder()
                .commentId(comment.getId())
                .actionType(FbCommentLog.ActionType.HIDE_COMMENT)
                .status(status)
                .responsePayload(payload)
                .createdAt(LocalDateTime.now())
                .build();
        fbCommentLogRepository.save(logEntry);
    }
}
