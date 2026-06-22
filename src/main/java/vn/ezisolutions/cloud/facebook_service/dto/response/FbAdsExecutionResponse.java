package vn.ezisolutions.cloud.facebook_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FbAdsExecutionResponse {
    private String planId;
    private String status;
    private int totalAds;
    private int successCount;
    private int failureCount;
    private List<String> successIds;
    private List<ErrorDetail> errors;

    @Data
    @Builder
    public static class ErrorDetail {
        private String adName;
        private String errorCode;
        private String errorMessage;
    }
}
