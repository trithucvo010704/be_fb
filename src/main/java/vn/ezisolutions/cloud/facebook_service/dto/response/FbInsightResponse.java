package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FbInsightResponse(
        List<InsightData> data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InsightData(
            String name,
            String period,
            String title,
            String description,
            List<InsightValue> values
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InsightValue(
            Integer value
    ) {}
}
