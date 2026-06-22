package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.campaign;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.Campaign;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCampaignRequest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FbAdsCampaignClientService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsCampaignClientService.class);
    private final FacebookErrorHandler fbErrorHandler;

    public Campaign createCampaignOnFacebook(String adAccountId, String token, FbCreateCampaignRequest request) {
        Callable<Campaign> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            Campaign.EnumObjective fbObjective = resolveObjective(request);
            String buyingType = request.getBuyingType() != null && !request.getBuyingType().isBlank()
                    ? request.getBuyingType()
                    : "AUCTION";

            var requestCreate = account.createCampaign()
                    .setName(request.getName())
                    .setObjective(fbObjective)
                    .setStatus(Campaign.EnumStatus.VALUE_PAUSED)
                    .setBuyingType(buyingType);

            List<Campaign.EnumSpecialAdCategories> categories = resolveSpecialAdCategories(request);
            requestCreate.setSpecialAdCategories(categories);

            applyCboSettings(requestCreate, request);

            return requestCreate.execute();
        };
        return fbErrorHandler.executeWithRetry(apiCall, "CREATE_CAMPAIGN", "AdAccount: " + adAccountId);
    }

    private Campaign.EnumObjective resolveObjective(FbCreateCampaignRequest request) {
        try {
            return Campaign.EnumObjective.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getObjective().name());
        } catch (Exception e) {
            logger.warn("Không khớp Objective {}, sử dụng mặc định OUTCOME_TRAFFIC", request.getObjective());
            return Campaign.EnumObjective.VALUE_OUTCOME_TRAFFIC;
        }
    }

    private List<Campaign.EnumSpecialAdCategories> resolveSpecialAdCategories(FbCreateCampaignRequest request) {
        if (request.getSpecialAdCategories() == null || request.getSpecialAdCategories().isEmpty()) {
            return Collections.singletonList(Campaign.EnumSpecialAdCategories.VALUE_NONE);
        }

        List<Campaign.EnumSpecialAdCategories> categories = request.getSpecialAdCategories()
                .stream()
                .map(this::mapSpecialAdCategory)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        if (categories.isEmpty()) {
            categories.add(Campaign.EnumSpecialAdCategories.VALUE_NONE);
        }
        return categories;
    }

    private Campaign.EnumSpecialAdCategories mapSpecialAdCategory(String category) {
        try {
            return Campaign.EnumSpecialAdCategories.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + category);
        } catch (Exception ex) {
            logger.warn("Không thể map SpecialAdCategory {}, bỏ qua", category);
            return null;
        }
    }

    private void applyCboSettings(AdAccount.APIRequestCreateCampaign requestCreate, FbCreateCampaignRequest request) {
        if (Boolean.TRUE.equals(request.getIsCbo()) && request.getDailyBudget() != null) {
            requestCreate.setDailyBudget(request.getDailyBudget());
            if (request.getBidStrategy() != null) {
                requestCreate.setBidStrategy(resolveBidStrategy(request));
            }
        } else {
            requestCreate.setParam("is_adset_budget_sharing_enabled", false);
        }
    }

    private Campaign.EnumBidStrategy resolveBidStrategy(FbCreateCampaignRequest request) {
        try {
            return Campaign.EnumBidStrategy.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getBidStrategy().name());
        } catch (Exception e) {
            logger.warn("Không khớp BidStrategy {}, sử dụng mặc định LOWEST_COST_WITH_BID_CAP", request.getBidStrategy());
            return Campaign.EnumBidStrategy.VALUE_LOWEST_COST_WITH_BID_CAP;
        }
    }
}
