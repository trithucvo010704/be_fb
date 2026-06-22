package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.creative;

import com.facebook.ads.sdk.AdCreative;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCreativeRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsCreative;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsCreativeRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FbAdsCreativeService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsCreativeService.class);
    private final FbAdsCreativeClientService clientService;
    private final FacebookTokenService tokenService;
    private final FbAdsAccountService accountService;
    private final FbAdsCreativeRepository creativeRepository;

    public String createCreative(String ownerId, FbCreateCreativeRequest request) throws CustomException {
        logger.info("createCreative start - adAccountId: {}, ownerId: {}", request.getAdAccountId(), ownerId);
        FbAdsAccount account = accountService.validateAndGetAccount(request.getAdAccountId(), ownerId);

        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null) {
            throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");
        }

        switch (request.getType()) {
            case MEDIA_IMAGE:
                if (request.getImageHash() == null || request.getImageHash().isBlank()) {
                    throw new CustomException(400, "Vui lòng cung cấp mã ảnh (image_hash) hợp lệ cho loại Creative hình ảnh.");
                }
                break;
            case MEDIA_VIDEO:
                if (request.getVideoId() == null || request.getVideoId().isBlank()) {
                    throw new CustomException(400, "Vui lòng cung cấp mã video (video_id) hợp lệ cho loại Creative video.");
                }
                break;
            case EXISTING_POST:
                if (request.getObjectStoryId() == null || request.getObjectStoryId().isBlank()) {
                    throw new CustomException(400, "Vui lòng cung cấp ID bài viết (object_story_id) hợp lệ cho loại Creative bài viết có sẵn.");
                }
                break;
            case CAROUSEL:
                throw new CustomException(400, "Loại Creative Quay vòng (Carousel) hiện chưa được hỗ trợ.");
        }

        try {
            logger.info("[CREATIVE-REQ] Type: {}, ImageHash: {}, VideoId: {}, PageId: {}, AdAccount: {}", 
                    request.getType(), request.getImageHash(), request.getVideoId(), request.getPageId(), request.getAdAccountId());
            
            AdCreative fbCreative = clientService.createCreativeOnFacebook(request.getAdAccountId(), token, request);

            FbAdsCreative newCreative = FbAdsCreative.builder()
                    .fbCreativeId(fbCreative.getId())
                    .adAccountId(request.getAdAccountId())
                    .pageId(request.getPageId())
                    .ownerId(ownerId)
                    .name(request.getName())
                    .type(request.getType())
                    .message(request.getMessage())
                    .headline(request.getHeadline())
                    .linkUrl(request.getLinkUrl())
                    .callToActionType(request.getCallToActionType())
                    .imageHash(request.getImageHash())
                    .videoId(request.getVideoId())
                    .objectStoryId(fbCreative.getFieldObjectStoryId())
                    .planId(request.getPlanId())
                    .assetId(request.getAssetId())
                    .status("ACTIVE")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            creativeRepository.save(newCreative);
            logger.info("Saved new creative to repository with ID: {}", newCreative.getFbCreativeId());
            logger.info("Tạo Creative thành công trên Facebook với ID: {}", fbCreative.getId());
            return fbCreative.getId();

        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo Creative cho tài khoản {}: {}", request.getAdAccountId(), e.getMessage());
            throw new CustomException(400, "Không thể tạo Mẫu quảng cáo trên Facebook: " + e.getMessage());
        }
    }
}
