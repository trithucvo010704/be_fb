package vn.ezisolutions.cloud.facebook_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbConnectedEvent {
    private String ownerId;
    private String pageId;
    private String pageName;
    private String userId;
    private List<String> permissions;
    private String status;
    private String connectedAt;
}
