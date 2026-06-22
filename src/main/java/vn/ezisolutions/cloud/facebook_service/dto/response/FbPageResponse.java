package vn.ezisolutions.cloud.facebook_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbPageResponse {
    private String fbPageId;
    private String pageName;
    private String category;
    private Set<String> pagePermissions;
    private String tokenStatus;
    private boolean isConnected;
    private String avatarUrl;
}
