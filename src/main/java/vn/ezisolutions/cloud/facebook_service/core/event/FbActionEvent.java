package vn.ezisolutions.cloud.facebook_service.core.event;

import lombok.Getter;

@Getter
public enum FbActionEvent {

    // Comment actions
    COMMENT_INBOX("INBOX"),
    COMMENT_REPLY("REPLY"),
    COMMENT_HIDE("HIDE"),

    // Webhook events
    WEBHOOK_COMMENT("facebook.webhook.comment"),
    WEBHOOK_REACTION("facebook.webhook.reaction"),
    // Page events
    PAGE_CONNECTED("facebook.page.connected"),
    SYNC_PAGE_HISTORY("facebook.page.sync.history"),
    SYNC_PAGE_INSIGHTS("facebook.page.sync.insights"),
    POSTING_PLAN("facebook.posting.plan"),
    AD_EXECUTION_RESULT("facebook.ad.execution.result");

    private final String value;

    FbActionEvent(String value) {
        this.value = value;
    }

    public static FbActionEvent fromValue(String value) {
        for (FbActionEvent status : FbActionEvent.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid FbActionEvent value: " + value);
    }
}
