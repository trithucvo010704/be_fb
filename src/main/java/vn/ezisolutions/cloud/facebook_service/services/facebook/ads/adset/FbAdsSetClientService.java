package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.adset;

import com.facebook.ads.sdk.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdSetRequest;

import java.util.List;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FbAdsSetClientService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsSetClientService.class);
    private final FacebookErrorHandler fbErrorHandler;

    public AdSet createAdSet(String adAccountId, String token, FbCreateAdSetRequest request) {
        Callable<AdSet> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            Targeting fbTargeting = buildTargeting(request);
            AdSet.EnumOptimizationGoal fbOptimizationGoal = resolveOptimizationGoal(request);
            AdSet.EnumBillingEvent fbBillingEvent = resolveBillingEvent(request);

            var requestCreate = account.createAdSet()
                    .setCampaignId(request.getCampaignId())
                    .setName(request.getName())
                    .setOptimizationGoal(fbOptimizationGoal)
                    .setBillingEvent(fbBillingEvent)
                    .setStatus(AdSet.EnumStatus.VALUE_PAUSED)
                    .setTargeting(fbTargeting);

            applyBudgetAndSchedule(requestCreate, request);
            applyBidStrategy(requestCreate, request);

            return requestCreate.execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "CREATE_ADSET", "AdAccount: " + adAccountId);
    }

    private Targeting buildTargeting(FbCreateAdSetRequest request) {
        Targeting fbTargeting = new Targeting();
        FbCreateAdSetRequest.FbTargetingRequest tReq = request.getTargeting();

        if (tReq != null) {
            if (tReq.getAgeMin() != null)
                fbTargeting.setFieldAgeMin(tReq.getAgeMin());
            if (tReq.getAgeMax() != null)
                fbTargeting.setFieldAgeMax(tReq.getAgeMax());
            if (tReq.getGenders() != null)
                fbTargeting.setFieldGenders(tReq.getGenders());
            if (tReq.getPublisherPlatforms() != null)
                fbTargeting.setFieldPublisherPlatforms(tReq.getPublisherPlatforms());
            if (tReq.getFacebookPositions() != null)
                fbTargeting.setFieldFacebookPositions(tReq.getFacebookPositions());

            if (tReq.getGeoLocations() != null && tReq.getGeoLocations().getCountries() != null) {
                TargetingGeoLocation geo = new TargetingGeoLocation();
                geo.setFieldCountries(tReq.getGeoLocations().getCountries());
                fbTargeting.setFieldGeoLocations(geo);
            }
        } else {
            TargetingGeoLocation geo = new TargetingGeoLocation();
            geo.setFieldCountries(List.of("VN"));
            fbTargeting.setFieldGeoLocations(geo);
        }
        return fbTargeting;
    }

    private AdSet.EnumOptimizationGoal resolveOptimizationGoal(FbCreateAdSetRequest request) {
        try {
            return AdSet.EnumOptimizationGoal.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getOptimizationGoal());
        } catch (Exception e) {
            return AdSet.EnumOptimizationGoal.VALUE_LINK_CLICKS;
        }
    }

    private AdSet.EnumBillingEvent resolveBillingEvent(FbCreateAdSetRequest request) {
        try {
            return AdSet.EnumBillingEvent.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getBillingEvent());
        } catch (Exception e) {
            return AdSet.EnumBillingEvent.VALUE_IMPRESSIONS;
        }
    }

    private void applyBudgetAndSchedule(AdAccount.APIRequestCreateAdSet requestCreate, FbCreateAdSetRequest request) {
        if (request.getDailyBudget() != null)
            requestCreate.setDailyBudget(request.getDailyBudget());
        if (request.getBidAmount() != null)
            requestCreate.setBidAmount(request.getBidAmount());
        if (request.getStartTime() != null)
            requestCreate.setStartTime(request.getStartTime());
        if (request.getEndTime() != null)
            requestCreate.setEndTime(request.getEndTime());
    }

    private void applyBidStrategy(AdAccount.APIRequestCreateAdSet requestCreate, FbCreateAdSetRequest request) {
        if (request.getBidStrategy() != null) {
            try {
                requestCreate.setBidStrategy(AdSet.EnumBidStrategy.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getBidStrategy()));
            } catch (Exception e) {
                logger.warn(
                        "[ADS-SET-CLIENT] Không khớp BidStrategy '{}' cho AdSet '{}', bỏ qua thiết lập (Facebook sẽ tự động kế thừa từ Campaign). Lỗi: {}",
                        request.getBidStrategy(), request.getName(), e.getMessage());
            }
        }
    }

    public void deleteAdSet(String adSetId, String token) {
        Callable<Void> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdSet adSet = new AdSet(adSetId, context);
            adSet.delete().execute();
            return null;
        };
        fbErrorHandler.executeWithRetry(apiCall, "DELETE_ADSET", "AdSet: " + adSetId);
    }
}
