package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FbConnectPageRequest {

    @NotBlank(message = "Thiếu fb_user_id")
    @JsonProperty("fb_user_id")
    private String fbUserId;

    @NotBlank(message = "Thiếu page_id")
    @JsonProperty("page_id")
    private String pageId;
}
