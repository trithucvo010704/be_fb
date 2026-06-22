package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.services.facebook.webhook.FacebookNotificationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/base/facebook/webhooks")
public class WebhookController {
    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final FacebookNotificationService notificationService;
    private final FacebookProperties facebookProperties;

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String token,
            @RequestParam("hub.challenge") String challenge) {
        if ("subscribe".equals(mode) && facebookProperties.getVerifyToken().equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        log.warn("Facebook webhook verification failed");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @PostMapping
    public BaseResponse receive(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestBody String payload) {
        notificationService.processWebhook(payload, signature);
        return BaseResponse.success("EVENT_RECEIVED");
    }
}
