package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.creative;

import com.facebook.ads.sdk.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.handlers.FacebookErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.shared.FacebookConstants;
import vn.ezisolutions.cloud.facebook_service.core.utils.StringUtils;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCreativeRequest;

import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FbAdsCreativeClientService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsCreativeClientService.class);
    private final FacebookErrorHandler fbErrorHandler;

    public AdCreative createCreativeOnFacebook(String adAccountId, String token, FbCreateCreativeRequest request) {
        Callable<AdCreative> apiCall = () -> {
            APIContext context = new APIContext(token);
            AdAccount account = new AdAccount("act_" + StringUtils.cleanId(adAccountId), context);

            var requestCreate = account.createAdCreative()
                    .setName(request.getName());

            String ctaType = request.getCallToActionType();
            boolean hasValidCta = ctaType != null
                    && !ctaType.trim().isEmpty()
                    && !ctaType.equalsIgnoreCase("NO_BUTTON");

            switch (request.getType()) {
                case EXISTING_POST:
                    requestCreate.setObjectStoryId(request.getObjectStoryId());
                    break;

                case MEDIA_IMAGE:
                    buildImageCreative(requestCreate, request, adAccountId, hasValidCta);
                    break;

                case MEDIA_VIDEO:
                    buildVideoCreative(requestCreate, request, adAccountId, hasValidCta);
                    break;

                case CAROUSEL:
                    logger.warn("Carousel creative requested but multi_share_data implementation is simplified.");
                    break;
            }

            return requestCreate.execute();
        };

        return fbErrorHandler.executeWithRetry(apiCall, "CREATE_CREATIVE", "AdAccount: " + adAccountId);
    }

    private void buildImageCreative(AdAccount.APIRequestCreateAdCreative requestCreate, FbCreateCreativeRequest request,
                                    String adAccountId, boolean hasValidCta) {
        AdCreativeLinkData linkData = new AdCreativeLinkData()
                .setFieldMessage(request.getMessage())
                .setFieldLink(request.getLinkUrl())
                .setFieldImageHash(request.getImageHash());

        if (request.getHeadline() != null) {
            linkData.setFieldName(request.getHeadline());
        }

        if (hasValidCta && request.getLinkUrl() != null) {
            AdCreativeLinkDataCallToAction cta = buildCtaForLink(request.getCallToActionType(), request.getLinkUrl());
            if (cta != null) {
                linkData.setFieldCallToAction(cta);
            }
        }

        AdCreativeObjectStorySpec specImage = new AdCreativeObjectStorySpec()
                .setFieldPageId(request.getPageId())
                .setFieldLinkData(linkData);

        logger.info("[SDK-DEBUG] Creating IMAGE Creative. PageId: {}, AdAccount: {}", request.getPageId(), adAccountId);
        requestCreate.setObjectStorySpec(specImage);
    }

    private void buildVideoCreative(AdAccount.APIRequestCreateAdCreative requestCreate, FbCreateCreativeRequest request,
                                    String adAccountId, boolean hasValidCta) {
        AdCreativeVideoData videoData = new AdCreativeVideoData()
                .setFieldVideoId(request.getVideoId())
                .setFieldImageHash(request.getImageHash())
                .setFieldMessage(request.getMessage());

        if (request.getHeadline() != null) {
            videoData.setFieldTitle(request.getHeadline());
        }

        if (hasValidCta && request.getLinkUrl() != null) {
            AdCreativeLinkDataCallToAction ctaVideo = buildCtaForLink(request.getCallToActionType(), request.getLinkUrl());
            if (ctaVideo != null) {
                videoData.setFieldCallToAction(ctaVideo);
            }
        }

        AdCreativeObjectStorySpec specVideo = new AdCreativeObjectStorySpec()
                .setFieldPageId(request.getPageId())
                .setFieldVideoData(videoData);

        logger.info("[SDK-DEBUG] Creating VIDEO Creative. PageId: {}, AdAccount: {}", request.getPageId(), adAccountId);
        requestCreate.setObjectStorySpec(specVideo);
    }

    private AdCreativeLinkDataCallToAction buildCtaForLink(String ctaType, String linkUrl) {
        try {
            return new AdCreativeLinkDataCallToAction()
                    .setFieldType(AdCreativeLinkDataCallToAction.EnumType.valueOf(FacebookConstants.Ads.FB_ENUM_PREFIX + ctaType.toUpperCase()))
                    .setFieldValue(new AdCreativeLinkDataCallToActionValue()
                            .setFieldLink(linkUrl));
        } catch (Exception ctaEx) {
            logger.warn("[CREATIVE-CTA] Không thể map CTA type {}, bỏ qua nút CTA: {}", ctaType, ctaEx.getMessage());
            return null;
        }
    }
}
