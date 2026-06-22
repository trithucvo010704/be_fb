package vn.ezisolutions.cloud.facebook_service.services.facebook.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.dto.webhook.FbWebhookPayload;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbNotification;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.enums.FbWebhookObjectType;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbNotificationRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacebookNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(FacebookNotificationService.class);

    private final FbNotificationRepository notificationRepo;
    private final FbPageRepository fbPageRepository;
    private final ObjectMapper objectMapper;
    private final FacebookProperties facebookProperties;
    private final List<IFbWebhookHandler> webhookHandlers;

    @Async("webhookTaskExecutor")
    public void processWebhook(String rawPayload, String signature) {
        try {
            validateSignature(rawPayload, signature);

            FbWebhookPayload payload = objectMapper.readValue(rawPayload, FbWebhookPayload.class);
            if (payload == null || payload.getEntry() == null) {
                return;
            }

            for (FbWebhookPayload.FbWebhookEntry entry : payload.getEntry()) {
                String pageId = entry.getId();
                if (pageId == null || !isPageActive(pageId)) {
                    logger.warn("Webhook Rejected: Page {} is inactive or unknown", pageId);
                    continue;
                }
                dispatchEntry(pageId, entry);
            }

        } catch (Exception e) {
            logger.error("Error processing webhook payload", e);
        }
    }

    private void dispatchEntry(String pageId, FbWebhookPayload.FbWebhookEntry entry) {
        if (entry.getMessaging() != null) {
            entry.getMessaging().forEach(msg -> processAndDispatch(pageId, FbWebhookObjectType.MESSAGING, msg));
        }

        if (entry.getChanges() != null) {
            entry.getChanges().forEach(change -> processAndDispatch(pageId, FbWebhookObjectType.FEED, change));
        }
    }

    private void processAndDispatch(String pageId, FbWebhookObjectType objectType, Object payloadNode) {
        String eventId = generateEventId(payloadNode);
        FbNotification notification = prepareNotification(pageId, objectType.toValue(), payloadNode, eventId);
        if (notification == null) {
            return;
        }
        try {
            boolean handled = false;
            for (IFbWebhookHandler handler : webhookHandlers) {
                if (handler.supports(objectType, payloadNode)) {
                    handler.handle(pageId, payloadNode, eventId);
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                logger.info("[Dispatch] No handler supported this event. EventId: {}, ObjectType: {}", eventId, objectType);
            }
            notification.setStatus(FbNotification.NotificationStatus.PROCESSED);
            notification.setErrorLog(null);
            notificationRepo.save(notification);
        } catch (Exception e) {
            notification.setStatus(FbNotification.NotificationStatus.FAILED);
            notification.setErrorLog(e.getMessage());
            notificationRepo.save(notification);
            logger.error("[Dispatch Error] EventId: {} - Reason: {}", eventId, e.getMessage(), e);
        }
    }

    private FbNotification prepareNotification(String pageId, String objectType, Object payloadNode, String eventId) {
        try {
            FbNotification notification = FbNotification.builder()
                    .eventId(eventId)
                    .objectType(objectType)
                    .pageId(pageId)
                    .payload(objectMapper.writeValueAsString(payloadNode))
                    .status(FbNotification.NotificationStatus.PROCESSING)
                    .receivedAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();
            return notificationRepo.saveAndFlush(notification);
        } catch (DataIntegrityViolationException ex) {
            return notificationRepo.findByEventId(eventId)
                    .filter(existing -> existing.getStatus() == FbNotification.NotificationStatus.RECEIVED
                            || existing.getStatus() == FbNotification.NotificationStatus.FAILED)
                    .map(existing -> {
                        existing.setStatus(FbNotification.NotificationStatus.PROCESSING);
                        existing.setErrorLog(null);
                        existing.setRetryCount(existing.getRetryCount() + 1);
                        return notificationRepo.save(existing);
                    })
                    .orElseGet(() -> {
                        logger.warn("Duplicate Event ID already processed or processing: {}", eventId);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("DB Save Error for eventId: {}", eventId, e);
            return null;
        }
    }

    private String generateEventId(Object payloadNode) {
        if (payloadNode instanceof FbWebhookPayload.FbWebhookMessaging messaging
                && messaging.getMessage() != null
                && messaging.getMessage().getMid() != null) {
            return messaging.getMessage().getMid();
        }
        return DigestUtils.sha256Hex(payloadNode.toString());
    }

    private void validateSignature(String payload, String signatureHeader) throws CustomException {
        if (signatureHeader == null || !signatureHeader.startsWith(FacebookConstants.Webhook.SHA256_PREFIX)) {
            throw new CustomException(401, "Invalid signature webhook");
        }

        String signature = signatureHeader.substring(FacebookConstants.Webhook.SHA256_PREFIX.length());
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
        byte[] secretBytes = facebookProperties.getAppSecret().getBytes(StandardCharsets.UTF_8);

        String expectedHash = new HmacUtils("HmacSHA256", secretBytes).hmacHex(payloadBytes);

        if (!expectedHash.equals(signature)) {
            throw new CustomException(403, "Webhook signature mismatch");
        }
    }

    private boolean isPageActive(String pageId) {
        return fbPageRepository.findByFbPageId(pageId)
                .map(p -> p.getTokenStatus() == FbPage.TokenStatus.ACTIVE
                        && Boolean.TRUE.equals(p.getWebhookSubscribed()))
                .orElse(false);
    }
}
