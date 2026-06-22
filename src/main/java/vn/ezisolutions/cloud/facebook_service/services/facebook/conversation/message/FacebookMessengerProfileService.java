package vn.ezisolutions.cloud.facebook_service.services.facebook.conversation.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookMessengerProfileResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookMessengerProfileGateway;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookMessengerProfileService {

    private static final String PROFILE_FIELDS = "first_name,last_name,name,profile_pic";

    private final FacebookMessengerProfileGateway profileGateway;
    private final FacebookTokenService tokenService;

    public void enrichIfMissing(FbConversation conversation, String fbPageId) {
        if (conversation == null || fbPageId == null || fbPageId.isBlank()) {
            return;
        }
        if (hasProfile(conversation)) {
            return;
        }
        String pageToken = tokenService.getPageAccessToken(fbPageId);
        if (pageToken == null || pageToken.isBlank()) {
            return;
        }
        try {
            FacebookMessengerProfileResponse profile = profileGateway.getProfile(
                    conversation.getSenderPsid(),
                    PROFILE_FIELDS,
                    pageToken
            );
            if (profile == null) {
                return;
            }
            conversation.setSenderName(profile.displayName());
            conversation.setSenderAvatarUrl(profile.profilePic());
            conversation.setRawProfile(toRawProfile(profile));
        } catch (Exception ignored) {
            // Profile enrichment is best-effort; message persistence must remain reliable.
        }
    }

    private boolean hasProfile(FbConversation conversation) {
        return conversation.getSenderName() != null && !conversation.getSenderName().isBlank()
                && conversation.getSenderAvatarUrl() != null && !conversation.getSenderAvatarUrl().isBlank();
    }

    private Map<String, Object> toRawProfile(FacebookMessengerProfileResponse profile) {
        Map<String, Object> raw = new LinkedHashMap<>();
        raw.put("id", profile.id());
        raw.put("first_name", profile.firstName());
        raw.put("last_name", profile.lastName());
        raw.put("name", profile.name());
        raw.put("profile_pic", profile.profilePic());
        return raw;
    }
}

