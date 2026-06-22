package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.comment;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.event.FbActionEvent;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbCommentEvent;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbComment;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbCommentRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

@Service
@RequiredArgsConstructor
public class FacebookCommentActionService {

    private static final Logger logger = LoggerFactory.getLogger(FacebookCommentActionService.class);
    private final FbPageRepository fbPageRepository;
    private final FbCommentRepository fbCommentRepository;
    private final FacebookInboxService facebookInboxService;
    private final FacebookReplyService facebookReplyService;
    private final FacebookSpamService facebookSpamService;

    private static final String COMMENT_NOT_FOUND_SUFFIX = " không tìm thấy trong DB!";

    public void processAction(FbActionEvent actionEvent, FbCommentEvent.CommentData data) throws CustomException {
        logger.info("[COMMENT-ACTION] Start processAction - action: {}, commentId: {}",
                actionEvent.getValue(), data.getFbCommentId());

        switch (actionEvent) {
            case COMMENT_INBOX:
                handleSendInbox(data);
                break;
            case COMMENT_REPLY:
                handleReplyComment(data);
                break;
            case COMMENT_HIDE:
                handleHideComment(data);
                break;
            default:
                logger.warn("[COMMENT-ACTION] Unhandled action event: {}", actionEvent);
        }

        logger.info("[COMMENT-ACTION] processAction success - action: {}, commentId: {}",
                actionEvent.getValue(), data.getFbCommentId());
    }

    private void handleSendInbox(FbCommentEvent.CommentData data) throws CustomException {
        String pageToken = getPageToken(data.getFbPageId());

        if (data.getParentId() != null) {
            throw new CustomException(400, "Không thể gửi Inbox cho Comment cấp 2 (Reply).");
        }

        FbComment comment = findCommentOrThrow(data.getFbCommentId());

        logger.info("[COMMENT-ACTION] Sending INBOX - commentId: {}", data.getFbCommentId());
        facebookInboxService.execute(comment.getId().toString(), data.getFbCommentId(), data.getMessage(), pageToken);
    }

    private void handleReplyComment(FbCommentEvent.CommentData data) throws CustomException {
        String pageToken = getPageToken(data.getFbPageId());

        FbComment comment = findCommentOrThrow(data.getFbCommentId());

        logger.info("[COMMENT-ACTION] Sending REPLY - commentId: {}", data.getFbCommentId());
        facebookReplyService.execute(comment.getId().toString(), data.getFbCommentId(), data.getMessage(), pageToken);
    }

    private void handleHideComment(FbCommentEvent.CommentData data) throws CustomException {
        String pageToken = getPageToken(data.getFbPageId());

        FbComment comment = findCommentOrThrow(data.getFbCommentId());

        logger.info("[COMMENT-ACTION] Sending HIDE - commentId: {}", data.getFbCommentId());
        facebookSpamService.execute(comment.getId().toString(), data.getFbCommentId(), pageToken);
    }

    private String getPageToken(String fbPageId) throws CustomException {
        FbPage page = fbPageRepository.findByFbPageId(fbPageId)
                .orElseThrow(() -> new CustomException(404, "Page ID " + fbPageId + " không tồn tại."));

        if (page.getPageAccessToken() == null) {
            throw new CustomException(404, "Page ID " + fbPageId + " mất Token.");
        }
        return page.getPageAccessToken();
    }

    private FbComment findCommentOrThrow(String fbCommentId) throws CustomException {
        return fbCommentRepository.findByFbCommentId(fbCommentId)
                .orElseThrow(() -> new CustomException(404,
                        "Comment " + fbCommentId + COMMENT_NOT_FOUND_SUFFIX));
    }
}
