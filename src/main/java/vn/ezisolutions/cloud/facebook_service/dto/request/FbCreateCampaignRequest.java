package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.enums.FbBidStrategy;
import vn.ezisolutions.cloud.facebook_service.enums.FbCampaignObjective;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbCreateCampaignRequest {

    @NotBlank(message = "Tên Campaign không được để trống")
    private String name;

    @NotBlank(message = "ID Tài khoản quảng cáo không được để trống")
    @JsonProperty("ad_account_id")
    private String adAccountId;

    @NotNull(message = "Mục tiêu không được để trống")
    private FbCampaignObjective objective;

    @JsonProperty("special_ad_categories")
    private List<String> specialAdCategories;

    @JsonProperty("is_cbo")
    private Boolean isCbo;

    @JsonProperty("daily_budget")
    private Long dailyBudget;

    @JsonProperty("bid_strategy")
    private FbBidStrategy bidStrategy;

    @JsonProperty("buying_type")
    private String buyingType;
}
