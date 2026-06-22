package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbCreateAdSetRequest {

    @NotBlank(message = "Tên AdSet không được để trống")
    private String name;

    @NotBlank(message = "ID Ad Account không được để trống")
    @JsonProperty("ad_account_id")
    private String adAccountId;

    @NotBlank(message = "ID Campaign cha không được để trống")
    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("plan_id")
    private String planId;

    @NotBlank(message = "Mục tiêu tối ưu không được để trống")
    @JsonProperty("optimization_goal")
    private String optimizationGoal;

    @NotBlank(message = "Sự kiện tính phí không được để trống")
    @JsonProperty("billing_event")
    private String billingEvent;

    @JsonProperty("bid_strategy")
    private String bidStrategy;

    @JsonProperty("bid_amount")
    private Long bidAmount;

    @JsonProperty("daily_budget")
    private Long dailyBudget;

    @JsonProperty("lifetime_budget")
    private Long lifetimeBudget;

    @NotNull(message = "Cấu hình Targeting không được để trống")
    private FbTargetingRequest targeting;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("promoted_object")
    private FbPromotedObjectRequest promotedObject;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FbPromotedObjectRequest {

        @JsonProperty("pixel_id")
        private String pixelId;

        @JsonProperty("custom_event_type")
        private String customEventType;

        @JsonProperty("page_id")
        private String pageId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FbTargetingRequest {

        @JsonProperty("age_min")
        private Long ageMin;

        @JsonProperty("age_max")
        private Long ageMax;

        private List<Long> genders;

        @JsonProperty("geo_locations")
        private GeoLocations geoLocations;

        @JsonProperty("is_auto_placement")
        private Boolean isAutoPlacement;

        @JsonProperty("device_platforms")
        private List<String> devicePlatforms;

        @JsonProperty("publisher_platforms")
        private List<String> publisherPlatforms;

        @JsonProperty("facebook_positions")
        private List<String> facebookPositions;

        @JsonProperty("instagram_positions")
        private List<String> instagramPositions;

        @JsonProperty("messenger_positions")
        private List<String> messengerPositions;

        @JsonProperty("audience_network_positions")
        private List<String> audienceNetworkPositions;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class GeoLocations {
            private List<String> countries;
        }
    }
}
