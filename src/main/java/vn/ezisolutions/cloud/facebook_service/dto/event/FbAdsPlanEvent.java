package vn.ezisolutions.cloud.facebook_service.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdSetRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCampaignRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCreativeRequest;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbAdsPlanEvent {
    @JsonProperty("plan_id")
    private String planId;

    @JsonProperty("owner_id")
    private String ownerId;

    @JsonProperty("fb_user_id")
    private String fbUserId;

    @JsonProperty("user_token")
    private String userToken;

    @JsonProperty("ad_account_id")
    private String adAccountId;

    @JsonProperty("page_id")
    private String pageId;

    @JsonProperty("campaign_id")
    private String campaignId;

    @JsonProperty("campaign_data")
    private FbCreateCampaignRequest campaignData;

    @JsonProperty("adsets")
    private List<AdsSetItem> adSets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdsSetItem {

        @JsonProperty("adset_id")
        private String adSetId;

        private String name;
        private String status;

        @JsonProperty("start_time")
        private String startTime;

        @JsonProperty("daily_budget")
        private Long dailyBudget;

        @JsonProperty("config_data")
        private AdSetConfigData configData;

        @JsonProperty("targeting")
        private FbCreateAdSetRequest.FbTargetingRequest targeting;

        @JsonProperty("ads")
        private List<AdsItem> ads;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AdsItem {
        private String name;
        private String status;

        @JsonProperty("creative_data")
        private FbCreateCreativeRequest creativeData;
    }
}
