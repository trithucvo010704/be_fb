package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPostRequest;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FbPostingPlanEvent {
    @JsonProperty("owner_id")
    private String ownerId;
    private List<FacebookPostRequest> posts;
}
