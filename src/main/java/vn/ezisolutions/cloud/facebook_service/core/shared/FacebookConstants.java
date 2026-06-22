package vn.ezisolutions.cloud.facebook_service.core.shared;

public class FacebookConstants {
    public static final String LOGIN_SCOPES = "public_profile,pages_show_list,pages_manage_metadata,pages_messaging,pages_manage_posts,pages_read_engagement";
    public static final String PROFILE_FIELDS = "id,name";
    public static final String PAGE_LIST_FIELDS = "id,name,access_token,category,tasks";
    public static final String WEBHOOK_SUBSCRIBE_FIELDS = "messages,messaging_postbacks,messaging_optins,feed";
    public static final String ENDPOINT_POST_INSIGHT = "post_media_view,post_engaged_users";
    public static final String ENDPOINT_POST_ENGAGEMENT_SUMMARY = "likes.summary(true).limit(0),comments.summary(true).limit(0),shares";
    public static final String ENDPOINT_POST_SYNC = "id,message,created_time,attachments{media_type,media,subattachments}";

    public static class Webhook {
        public static final String SHA256_PREFIX = "sha256=";
        public static final String VERB_ADD = "add";
        public static final String VERB_EDIT = "edit";
        
        private Webhook() {}
    }

    public static class GraphApi {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String MESSAGE = "message";
        public static final String PUBLISHED = "published";
        public static final String DESCRIPTION = "description";
        public static final String VIDEO_ID = "video_id";
        public static final String UPLOAD_PHASE = "upload_phase";
        public static final String VIDEO_STATE = "video_state";
        public static final String SCHEDULED_PUBLISH_TIME = "scheduled_publish_time";
        public static final String FILE_URL = "file_url";

        private GraphApi() {}
    }

    public static class Ads {
        public static final String FB_ENUM_PREFIX = "VALUE_";

        private Ads() {}
    }
}
