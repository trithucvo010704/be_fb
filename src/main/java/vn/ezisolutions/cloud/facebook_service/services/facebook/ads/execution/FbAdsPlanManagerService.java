package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.execution;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbAdsPlanEvent;
import vn.ezisolutions.cloud.facebook_service.dto.response.FbAdsExecutionResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsPlan;
import vn.ezisolutions.cloud.facebook_service.enums.PlanStatus;
import vn.ezisolutions.cloud.facebook_service.enums.PlanStep;
import vn.ezisolutions.cloud.facebook_service.listeners.FbAdsResultProducer;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsPlanRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FbAdsPlanManagerService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsPlanManagerService.class);

    private final FbAdsPlanRepository planRepository;
    private final FbAdsResultProducer resultProducer;

    public FbAdsPlan initializePlan(FbAdsPlanEvent event) {
        FbAdsPlan plan = planRepository.findByPlanId(event.getPlanId()).orElseGet(() -> FbAdsPlan.builder()
                .planId(event.getPlanId())
                .ownerId(event.getOwnerId())
                .adAccountId(event.getAdAccountId())
                .pageId(event.getPageId())
                .createdAt(LocalDateTime.now())
                .build());

        int totalExpectedAds = 0;
        if (event.getAdSets() != null) {
            totalExpectedAds = event.getAdSets().stream()
                    .filter(set -> set.getAds() != null)
                    .mapToInt(set -> set.getAds().size())
                    .sum();
        }

        plan.setStatus(PlanStatus.PROCESSING);
        plan.setCurrentStep(PlanStep.START);
        plan.setTotalAds(totalExpectedAds);
        plan.setSuccessCount(0);
        plan.setFailureCount(0);
        plan.setUpdatedAt(LocalDateTime.now());
        return planRepository.save(plan);
    }

    public void updateStep(FbAdsPlan plan, PlanStep step) {
        plan.setCurrentStep(step);
        planRepository.save(plan);
    }

    public void saveCampaignId(FbAdsPlan plan, String campaignId) {
        plan.setCampaignId(campaignId);
        planRepository.save(plan);
    }

    public void saveProgress(FbAdsPlan plan, FbAdsExecutionContext context) {
        plan.setSuccessCount(context.getSuccessCounter());
        plan.setFailureCount(context.getFailureCounter());
        planRepository.save(plan);
    }

    public void finalizeExecution(FbAdsPlan plan, FbAdsExecutionContext context) {
        if (context.getFailureCounter() == 0 && context.getSuccessCounter() > 0) {
            plan.setStatus(PlanStatus.SUCCESS);
        } else if (context.getSuccessCounter() > 0) {
            plan.setStatus(PlanStatus.PARTIAL_FAILED);
        } else {
            plan.setStatus(PlanStatus.FAILED);
            plan.setErrorMessage("Toàn bộ AdSet/Ad đều tạo thất bại.");
        }

        plan.setCurrentStep(PlanStep.COMPLETED);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);

        if (resultProducer != null) {
            FbAdsExecutionResponse response = FbAdsExecutionResponse.builder()
                    .planId(plan.getPlanId())
                    .status(plan.getStatus().name())
                    .totalAds(plan.getTotalAds())
                    .successCount(context.getSuccessCounter())
                    .failureCount(context.getFailureCounter())
                    .successIds(context.getSuccessIds())
                    .errors(context.getErrors())
                    .build();
            try {
                resultProducer.sendExecutionResult(response);
            } catch (Exception e) {
                logger.error("[ADS-PLAN-MANAGER] Failed to send finalize event to Kafka for planId: {}, error: {}",
                        plan.getPlanId(), e.getMessage(), e);
            }
        }
    }

    public void handleFatalError(FbAdsPlan plan, String errorMsg) {
        plan.setStatus(PlanStatus.FAILED);
        plan.setCurrentStep(PlanStep.COMPLETED);
        plan.setErrorMessage(errorMsg);
        plan.setUpdatedAt(LocalDateTime.now());
        planRepository.save(plan);

        if (resultProducer != null) {
            FbAdsExecutionResponse response = FbAdsExecutionResponse.builder()
                    .planId(plan.getPlanId())
                    .status(PlanStatus.FAILED.name())
                    .successCount(0)
                    .failureCount(1)
                    .errors(List.of(FbAdsExecutionResponse.ErrorDetail.builder()
                            .adName("ALL_ADS")
                            .errorCode("SYSTEM_ERROR")
                            .errorMessage(errorMsg)
                            .build()))
                    .build();
            try {
                resultProducer.sendExecutionResult(response);
            } catch (Exception e) {
                logger.error("[ADS-PLAN-MANAGER] Failed to send fatal error event to Kafka for planId: {}, error: {}",
                        plan.getPlanId(), e.getMessage(), e);
            }
        }
    }
}
