package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbSyncInsightEvent {
    @JsonProperty("owner_id")
    private String ownerId;
    private String pageId;
    private List<String> postIds;
}
