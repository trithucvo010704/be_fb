package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FbPageInfoResponse(
        String id,
        String name,

        @JsonProperty("access_token")
        String accessToken,

        String category,
        List<String> tasks
) {}
