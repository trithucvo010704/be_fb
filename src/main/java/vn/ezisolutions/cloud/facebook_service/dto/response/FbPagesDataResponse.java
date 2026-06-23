package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FbPagesDataResponse(
        List<FbPageInfoResponse> data,
        Paging paging
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Paging(String next) {
    }
}
