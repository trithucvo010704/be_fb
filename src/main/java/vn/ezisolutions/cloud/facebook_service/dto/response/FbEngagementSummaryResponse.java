package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FbEngagementSummaryResponse(
        EngagementNode likes,
        EngagementNode comments,
        EngagementNode shares
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EngagementNode(
            Summary summary,
            Integer count
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Summary(
            @JsonProperty("total_count") Integer totalCount
    ) {}
}
