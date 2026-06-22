package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.execution;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.event.AdSetConfigData;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbAdsPlanEvent;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdSetRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCampaignRequest;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCreativeRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsPlan;
import vn.ezisolutions.cloud.facebook_service.enums.PlanStep;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.ad.FbAdsService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.adset.FbAdsSetService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.campaign.FbAdsCampaignService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.creative.FbAdsCreativeService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FbAdsOrchestratorService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsOrchestratorService.class);

    private final FbAdsCampaignService campaignService;
    private final FbAdsSetService adSetService;
    private final FbAdsCreativeService creativeService;
    private final FbAdsService adService;
    private final FbAdsPlanManagerService planManager;

    public void executeRelease(FbAdsPlanEvent event) {
        String planId = event.getPlanId();
        logger.info("[ORCHESTRATOR] Bắt đầu xử lý Plan {} cho Account {}", planId, event.getAdAccountId());

        FbAdsPlan plan = planManager.initializePlan(event);
        FbAdsExecutionContext context = new FbAdsExecutionContext();

        try {
            String campaignId = handleCampaignStep(plan, event);
            if (campaignId == null)
                return;

            handleAdSetsAndAds(plan, event, campaignId, context);

            planManager.finalizeExecution(plan, context);

            logger.info("[ORCHESTRATOR] Hoàn tất xử lý Plan {}. Status: {}", planId, plan.getStatus());

        } catch (Exception e) {
            logger.error("[ORCHESTRATOR] Lỗi nghiêm trọng trong tiến trình Plan {}: {}", planId, e.getMessage(), e);
            planManager.handleFatalError(plan, "Lỗi hệ thống trong tiến trình: " + e.getMessage());
        }
    }

    private String handleCampaignStep(FbAdsPlan plan, FbAdsPlanEvent event) {
        planManager.updateStep(plan, PlanStep.CAMPAIGN);

        String campaignId = event.getCampaignId();
        if (campaignId == null || campaignId.isBlank()) {
            if (event.getCampaignData() != null) {
                try {
                    FbCreateCampaignRequest campaignReq = event.getCampaignData();
                    campaignReq.setAdAccountId(event.getAdAccountId());
                    campaignId = campaignService.createCampaign(event.getOwnerId(), campaignReq);
                    logger.info("[ORCHESTRATOR] Tạo Campaign mới thành công: {}", campaignId);
                    planManager.saveCampaignId(plan, campaignId);
                } catch (Exception campEx) {
                    logger.error("[ORCHESTRATOR] Lỗi tạo Campaign cho Plan {}: {}", plan.getPlanId(), campEx.getMessage());
                    planManager.handleFatalError(plan, "Lỗi tạo Campaign: " + campEx.getMessage());
                    return null;
                }
            } else {
                planManager.handleFatalError(plan, "Không có campaign_id và Không có campaign_data để tạo mới.");
                return null;
            }
        } else {
            logger.info("[ORCHESTRATOR] Sử dụng Campaign ID có sẵn: {}", campaignId);
            planManager.saveCampaignId(plan, campaignId);
        }
        return campaignId;
    }

    private void handleAdSetsAndAds(FbAdsPlan plan, FbAdsPlanEvent event, String campaignId,
            FbAdsExecutionContext context) {
        planManager.updateStep(plan, PlanStep.AD_SETS);

        List<FbAdsPlanEvent.AdsSetItem> adSets = event.getAdSets();
        if (adSets == null || adSets.isEmpty()) {
            planManager.handleFatalError(plan, "Không có AdSet nào được định nghĩa trong Plan.");
            return;
        }

        for (FbAdsPlanEvent.AdsSetItem setItem : adSets) {
            processAdSetItem(plan, event, campaignId, context, setItem);
            planManager.saveProgress(plan, context);
        }
    }

    private void processAdSetItem(FbAdsPlan plan, FbAdsPlanEvent event, String campaignId,
                                  FbAdsExecutionContext context, FbAdsPlanEvent.AdsSetItem setItem) {
        String adSetId = null;
        try {
            FbCreateAdSetRequest adSetReq = buildAdSetReq(setItem, event.getAdAccountId(), campaignId,
                    plan.getPlanId());
            adSetId = adSetService.createAdSet(event.getOwnerId(), adSetReq);
            logger.info("[ORCHESTRATOR] Tạo AdSet thành công: {}", adSetId);

            List<FbAdsPlanEvent.AdsItem> ads = setItem.getAds();
            int totalAdsInSet = ads != null ? ads.size() : 0;
            int successAdsInSet = 0;

            if (ads != null && !ads.isEmpty()) {
                for (FbAdsPlanEvent.AdsItem adItem : ads) {
                    boolean adSuccess = handleSingleAd(plan, event, adSetId, adItem, context);
                    if (adSuccess) {
                        successAdsInSet++;
                    }
                }
            } else {
                logger.warn("[ORCHESTRATOR] AdSet {} không chứa Ads nào.", adSetId);
            }

            cleanupOrphanAdSet(event, adSetId, totalAdsInSet, successAdsInSet);

        } catch (Exception ex) {
            logger.error("[ORCHESTRATOR] Lỗi tạo AdSet cho plan {}: {}", plan.getPlanId(), ex.getMessage());
            if (setItem.getAds() != null) {
                for (FbAdsPlanEvent.AdsItem adItem : setItem.getAds()) {
                    context.addFailure(adItem.getName(), "CREATE_ADSET_ERROR",
                            "Lỗi tạo AdSet cha: " + ex.getMessage());
                }
            }
        }
    }

    private void cleanupOrphanAdSet(FbAdsPlanEvent event, String adSetId, int totalAdsInSet, int successAdsInSet) {
        if (totalAdsInSet > 0 && successAdsInSet == 0 && adSetId != null) {
            logger.warn("[ORCHESTRATOR] Toàn bộ {} Ads của AdSet {} đều thất bại. Tiến hành dọn dẹp AdSet mồ côi để tránh làm bẩn tài khoản.", totalAdsInSet, adSetId);
            try {
                adSetService.deleteAdSetOnFacebook(event.getOwnerId(), event.getAdAccountId(), adSetId);
                logger.info("[ORCHESTRATOR] Dọn dẹp AdSet mồ côi {} thành công.", adSetId);
            } catch (Exception cleanEx) {
                logger.error("[ORCHESTRATOR] Không thể dọn dẹp AdSet {} do lỗi: {}", adSetId, cleanEx.getMessage());
            }
        }
    }

    private boolean handleSingleAd(FbAdsPlan plan, FbAdsPlanEvent event, String adSetId, FbAdsPlanEvent.AdsItem adItem,
            FbAdsExecutionContext context) {
        try {
            String creativeId = processCreative(event.getOwnerId(), event.getAdAccountId(), event.getPageId(),
                    plan.getPlanId(), adItem);
            if (creativeId != null) {
                FbCreateAdRequest adReq = FbCreateAdRequest.builder()
                        .name(adItem.getName() != null ? adItem.getName() : "Auto Ad")
                        .adAccountId(event.getAdAccountId())
                        .adSetId(adSetId)
                        .creativeId(creativeId)
                        .planId(plan.getPlanId())
                        .status(adItem.getStatus() != null ? adItem.getStatus() : "PAUSED")
                        .build();

                String finalAdId = adService.createAd(event.getOwnerId(), adReq);
                logger.info("[ORCHESTRATOR] Tạo Ad thành công: {}", finalAdId);
                context.addSuccess(finalAdId);
                return true;
            } else {
                throw new CustomException(400, "Không thể xử lý Creative_data (Null)");
            }
        } catch (Exception adEx) {
            logger.error("[ORCHESTRATOR] Lỗi tạo Ad/Creative cho plan {} - Ad name {}: {}", plan.getPlanId(),
                    adItem.getName(), adEx.getMessage());
            context.addFailure(adItem.getName(), "CREATE_AD_ERROR", adEx.getMessage());
            return false;
        }
    }

    private FbCreateAdSetRequest buildAdSetReq(FbAdsPlanEvent.AdsSetItem setItem, String adAccountId, String campaignId,
            String planId) {
        FbCreateAdSetRequest adSetReq = new FbCreateAdSetRequest();
        adSetReq.setName(setItem.getName());
        adSetReq.setAdAccountId(adAccountId);
        adSetReq.setCampaignId(campaignId);
        adSetReq.setPlanId(planId);

        if (setItem.getConfigData() != null) {
            AdSetConfigData config = setItem.getConfigData();

            if (config.getOptimizationGoal() != null)
                adSetReq.setOptimizationGoal(config.getOptimizationGoal());

            if (config.getBillingEvent() != null)
                adSetReq.setBillingEvent(config.getBillingEvent());

            if (config.getBidStrategy() != null)
                adSetReq.setBidStrategy(config.getBidStrategy());

            if (config.getBidAmount() != null)
                adSetReq.setBidAmount(config.getBidAmount());
        }

        adSetReq.setStartTime(setItem.getStartTime());
        adSetReq.setDailyBudget(setItem.getDailyBudget());

        if (setItem.getTargeting() != null) {
            adSetReq.setTargeting(setItem.getTargeting());
        }
        return adSetReq;
    }

    private String processCreative(String ownerId, String adAccountId, String pageId, String planId,
            FbAdsPlanEvent.AdsItem adItem) throws CustomException {
        FbCreateCreativeRequest creativeReq = adItem.getCreativeData();
        if (creativeReq == null) {
            throw new CustomException(400, "AdItem thiếu creative_data");
        }

        creativeReq.setAdAccountId(adAccountId);
        creativeReq.setPageId(pageId);
        creativeReq.setPlanId(planId);
        return creativeService.createCreative(ownerId, creativeReq);
    }
}
