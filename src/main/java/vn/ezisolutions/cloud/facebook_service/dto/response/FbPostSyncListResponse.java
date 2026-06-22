package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FbPostSyncListResponse(
        List<FbPostSyncDataResponse> data,
        Paging paging
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(Cursors cursors, String next) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cursors(String before, String after) {}
}
