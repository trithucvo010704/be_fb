package vn.ezisolutions.cloud.facebook_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemoLoginRequest {
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Password không được để trống")
    private String password;
}
