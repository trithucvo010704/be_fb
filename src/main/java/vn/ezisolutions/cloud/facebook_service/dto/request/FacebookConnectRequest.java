package vn.ezisolutions.cloud.facebook_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FacebookConnectRequest {
    @NotBlank(message = "Facebook Access Token không được để trống")
    private String fbAccessToken;
}
