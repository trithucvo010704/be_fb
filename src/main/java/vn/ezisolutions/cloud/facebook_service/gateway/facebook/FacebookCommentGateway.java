package vn.ezisolutions.cloud.facebook_service.gateway.facebook;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbCommentHideResponse;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbCommentResponse;

@HttpExchange
public interface FacebookCommentGateway {

    @PostExchange("/{commentId}/comments")
    FbCommentResponse replyToComment(
            @PathVariable("commentId") String commentId,
            @RequestParam("message") String message,
            @RequestParam("access_token") String accessToken
    );

    @PostExchange("/{commentId}")
    FbCommentHideResponse hideComment(
            @PathVariable("commentId") String commentId,
            @RequestParam("is_hidden") boolean isHidden,
            @RequestParam("access_token") String accessToken
    );
}
