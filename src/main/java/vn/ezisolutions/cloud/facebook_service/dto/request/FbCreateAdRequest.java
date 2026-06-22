package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbCreateAdRequest {

    @NotBlank(message = "Tên Ad không được để trống")
    private String name;

    @NotBlank(message = "ID Ad Account không được để trống")
    @JsonProperty("ad_account_id")
    private String adAccountId;

    @NotBlank(message = "ID AdSet cha không được để trống")
    @JsonProperty("adset_id")
    private String adSetId;

    @NotBlank(message = "ID Creative không được để trống")
    @JsonProperty("creative_id")
    private String creativeId;

    @JsonProperty("plan_id")
    private String planId;

    private String status; // PAUSED / ACTIVE

    @JsonProperty("tracking_specs")
    private List<Map<String, Object>> trackingSpecs;
}
