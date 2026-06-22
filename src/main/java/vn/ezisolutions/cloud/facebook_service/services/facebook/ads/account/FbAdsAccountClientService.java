package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;

import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FbAdsAccountClientService {
    private final FacebookErrorHandler fbErrorHandler;

    public APINodeList<AdAccount> fetchAdAccountsFromFacebook(String fbUserId, String token) {
        Callable<APINodeList<AdAccount>> apiCall = () -> {
            APIContext context = new APIContext(token);
            User me = new User(fbUserId, context);

            return me.getAdAccounts()
                    .requestField("id")
                    .requestField("name")
                    .requestField("currency")
                    .requestField("timezone_name")
                    .requestField("account_status")
                    .requestField("balance")
                    .execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "FETCH_AD_ACCOUNTS", "FbUserId: " + fbUserId);
    }
}
