package vn.ezisolutions.cloud.facebook_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbAdsGraphResponse {
    private String id;
    private Boolean success;
    private FbError error;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FbError {
        private String message;
        private String type;
        private int code;
        @JsonProperty("error_subcode")
        private Integer errorSubcode;

        @JsonProperty("fbtrace_id")
        private String fbTraceId;
    }
}
