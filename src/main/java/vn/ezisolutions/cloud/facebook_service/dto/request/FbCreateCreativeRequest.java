package vn.ezisolutions.cloud.facebook_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsCreative.FbCreativeType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FbCreateCreativeRequest {

    @NotBlank(message = "Tên Creative không được để trống")
    private String name;

    @NotBlank(message = "ID Tài khoản quảng cáo không được để trống")
    @JsonProperty("ad_account_id")
    private String adAccountId;

    @NotBlank(message = "ID Fanpage không được để trống")
    @JsonProperty("page_id")
    private String pageId;

    @NotNull(message = "Loại Creative không được để trống")
    private FbCreativeType type;

    private String message;

    private String headline;

    @JsonProperty("link_url")
    private String linkUrl;

    @JsonProperty("call_to_action_type")
    private String callToActionType;

    @JsonProperty("media_url")
    private String mediaUrl;

    @JsonProperty("image_hash")
    private String imageHash;

    @JsonProperty("video_id")
    private String videoId;

    @JsonProperty("object_story_id")
    private String objectStoryId;

    @JsonProperty("asset_id")
    private String assetId;

    @JsonProperty("plan_id")
    private String planId;
}
