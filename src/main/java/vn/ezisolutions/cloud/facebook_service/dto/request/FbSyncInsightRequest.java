package vn.ezisolutions.cloud.facebook_service.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class FbSyncInsightRequest {
    @NotNull(message = "Thiếu danh sách postIds")
    @NotEmpty(message = "Danh sách postIds không được để trống")
    private List<String> postIds;
}
