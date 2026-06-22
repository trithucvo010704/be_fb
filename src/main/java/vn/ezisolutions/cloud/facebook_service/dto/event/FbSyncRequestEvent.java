package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FbSyncRequestEvent {
    @JsonProperty("owner_id")
    private String ownerId;
}
