package vn.ezisolutions.cloud.facebook_service.core.shared;

public class RedisKeys {
    public static final String TOKEN_KEY = "id_system:tokens";
    // Facebook Caching
    public static final String TOKEN_REGEX = "id_system:tokens:%s";
    public static final String FB_PREFIX = "heyezi";
    public static final String NS_USER_TOKENS = "user_tokens";
    public static final String NS_PAGE_TOKENS = "page_tokens";
    public static final String EMPTY_TOKEN_MARKER = "EMPTY";
    public static final String SIMULATOR_PROFILE_DEVICE = "simulator:profile_device";
}
