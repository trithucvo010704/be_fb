package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.utils.SecurityUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDataDeletionRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDisconnectPageRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPublishPostRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookSendMessageRequest;
import vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message.FacebookMessengerInboxService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message.FacebookMessengerService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookDataDeletionService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageDisconnectService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.FacebookPagePublishService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class FacebookReviewController {

    private final FacebookMessengerInboxService inboxService;
    private final FacebookMessengerService messengerService;
    private final FacebookPagePublishService publishService;
    private final FacebookPageDisconnectService disconnectService;
    private final FacebookDataDeletionService dataDeletionService;

    @GetMapping("/api/pages/{pageId}/conversations")
    public BaseResponse conversations(
            @PathVariable String pageId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) throws CustomException {
        return BaseResponse.success(inboxService.listConversations(pageId, page, limit));
    }

    @GetMapping("/api/pages/{pageId}/conversations/{conversationId}/messages")
    public BaseResponse messages(
            @PathVariable String pageId,
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit
    ) throws CustomException {
        return BaseResponse.success(inboxService.listMessages(pageId, conversationId, page, limit));
    }

    @PostMapping("/api/pages/{pageId}/messages")
    public BaseResponse sendMessage(
            @PathVariable String pageId,
            @Valid @RequestBody FacebookSendMessageRequest request
    ) throws CustomException {
        return BaseResponse.success("MESSAGE_SENT", messengerService.sendMessage(pageId, request, currentUser()));
    }

    @PostMapping("/api/pages/{pageId}/posts")
    public BaseResponse publishPost(
            @PathVariable String pageId,
            @Valid @RequestBody FacebookPublishPostRequest request
    ) throws CustomException {
        return BaseResponse.success("POST_PUBLISHED", publishService.publish(pageId, request, currentUser()));
    }

    @GetMapping("/api/pages/{pageId}/posts")
    public BaseResponse posts(
            @PathVariable String pageId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) throws CustomException {
        return BaseResponse.success(publishService.listPosts(pageId, page, limit));
    }

    @PostMapping("/api/pages/{pageId}/disconnect")
    public BaseResponse disconnect(
            @PathVariable String pageId,
            @RequestBody(required = false) FacebookDisconnectPageRequest request
    ) throws CustomException {
        return BaseResponse.success("PAGE_DISCONNECTED", disconnectService.disconnect(pageId, request, currentUser()));
    }

    @PostMapping(value = "/api/data-deletion", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResponse dataDeletionJson(@RequestBody(required = false) FacebookDataDeletionRequest request) throws CustomException {
        return BaseResponse.success("DATA_DELETION_RECEIVED", dataDeletionService.receive(request));
    }

    @PostMapping(value = "/api/data-deletion", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public BaseResponse dataDeletionForm(@RequestParam MultiValueMap<String, String> form) throws CustomException {
        FacebookDataDeletionRequest request = new FacebookDataDeletionRequest(
                form.getFirst("requester_email"),
                form.getFirst("fb_user_id"),
                form.getFirst("fb_page_id"),
                form.getFirst("signed_request"),
                form.getFirst("note")
        );
        return BaseResponse.success("DATA_DELETION_RECEIVED", dataDeletionService.receive(request));
    }

    private AuthorizedUser currentUser() throws CustomException {
        return SecurityUtils.getCurrentAuthorizedUser()
                .orElseThrow(() -> new CustomException(401, "Unauthenticated"));
    }
}
