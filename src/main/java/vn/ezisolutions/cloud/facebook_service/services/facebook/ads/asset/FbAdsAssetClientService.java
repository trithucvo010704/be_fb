package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.asset;

import com.facebook.ads.sdk.APIContext;
import com.facebook.ads.sdk.AdAccount;
import com.facebook.ads.sdk.AdImage;
import com.facebook.ads.sdk.AdVideo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;

import java.io.File;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FbAdsAssetClientService {
    private final FacebookErrorHandler fbErrorHandler;

    public AdImage uploadImageToFacebook(String adAccountId, String token, File imageFile) {
        Callable<AdImage> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            return account.createAdImage()
                    .addUploadFile("filename", imageFile)
                    .execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "UPLOAD_AD_IMAGE", "AdAccount: " + adAccountId);
    }

    public AdVideo uploadVideoToFacebook(String adAccountId, String token, File videoFile, String title) {
        Callable<AdVideo> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            return account.createAdVideo()
                    .addUploadFile("source", videoFile)
                    .setTitle(title != null ? title : "EZI Ad Video")
                    .execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "UPLOAD_AD_VIDEO", "AdAccount: " + adAccountId);
    }

    public AdVideo fetchVideoDetails(String videoId, String token) {
        Callable<AdVideo> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdVideo video = new AdVideo(videoId, context);

            return video.get()
                    .requestField("status")
                    .requestField("picture")
                    .requestField("length")
                    .execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "FETCH_VIDEO_DETAILS", "VideoId: " + videoId);
    }
}
