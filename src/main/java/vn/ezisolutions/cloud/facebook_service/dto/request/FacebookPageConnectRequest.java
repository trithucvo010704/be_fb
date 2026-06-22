package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FacebookPageConnectRequest {
    @NotBlank(message = "User access token không được để trống")
    @JsonProperty("user_access_token")
    private String userAccessToken;
}
