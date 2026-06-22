package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.ad;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.Ad;
import com.facebook.ads.sdk.AdAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdRequest;

import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FbAdsClientService {
    private final FacebookErrorHandler fbErrorHandler;

    public Ad createAdOnFacebook(String adAccountId, String token, FbCreateAdRequest request) {
        Callable<Ad> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            var requestCreate = account.createAd()
                    .setName(request.getName())
                    .setAdsetId(request.getAdSetId())
                    .setCreative(new com.facebook.ads.sdk.AdCreative().setFieldId(request.getCreativeId()))
                    .setStatus(request.getStatus() != null
                            ? Ad.EnumStatus.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + request.getStatus().toUpperCase())
                            : Ad.EnumStatus.VALUE_PAUSED);

            if (request.getTrackingSpecs() != null && !request.getTrackingSpecs().isEmpty()) {
                requestCreate.setTrackingSpecs(request.getTrackingSpecs().toString());
            }

            return requestCreate.execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "CREATE_AD", "AdAccount: " + adAccountId);
    }
}
