package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.execution;

import lombok.Getter;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbAdsExecutionResponse;

import java.util.ArrayList;
import java.util.List;

@Getter
public class FbAdsExecutionContext {
    private int successCounter = 0;
    private int failureCounter = 0;
    private final List<String> successIds = new ArrayList<>();
    private final List<FbAdsExecutionResponse.ErrorDetail> errors = new ArrayList<>();

    public void addSuccess(String adId) {
        this.successCounter++;
        this.successIds.add(adId);
    }

    public void addFailure(String adName, String errorCode, String errorMessage) {
        this.failureCounter++;
        this.errors.add(FbAdsExecutionResponse.ErrorDetail.builder()
                .adName(adName)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .build());
    }
}
