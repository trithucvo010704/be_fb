package vn.ezisolutions.cloud.facebook_service.services.facebook.webhook;

import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.enums.FbWebhookObjectType;

public interface IFbWebhookHandler {
    boolean supports(FbWebhookObjectType objectType, Object payload);
    void handle(String pageId, Object payload, String eventId) throws CustomException;
}
