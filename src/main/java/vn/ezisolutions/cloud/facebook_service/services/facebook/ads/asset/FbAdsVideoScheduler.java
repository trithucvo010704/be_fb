package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.asset;

import com.facebook.ads.sdk.AdVideo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAsset;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsAssetRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@ConditionalOnProperty(prefix = "facebook.features.ads", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FbAdsVideoScheduler {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsVideoScheduler.class);
    private final FbAdsAssetRepository assetRepo;
    private final FbAdsAssetClientService assetClientService;
    private final FbAdsAccountService accountService;
    private final FacebookTokenService tokenService;

    @Scheduled(fixedDelay = 60000)
    public void checkProcessingVideos() {
        List<FbAdsAsset> processingVideos = assetRepo.findByStatusAndType(
                FbAdsAsset.AssetStatus.PROCESSING,
                FbAdsAsset.AssetType.VIDEO
        );

        if (processingVideos.isEmpty()) {
            return;
        }
        logger.info("Cronjob: Tìm thấy {} video đang chờ Facebook xử lý...", processingVideos.size());
        for (FbAdsAsset asset : processingVideos) {
            try {
                FbAdsAccount account = accountService.validateAndGetAccount(asset.getAdAccountId(), asset.getOwnerId());
                String token = tokenService.getUserAccessToken(account.getFbUserId());
                if (token == null) continue;
                AdVideo fbVideo = assetClientService.fetchVideoDetails(asset.getFbAssetId(), token);
                String thumbnailUrl = fbVideo.getFieldPicture();
                Double length = fbVideo.getFieldLength();

                if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
                    asset.setThumbnailUrl(thumbnailUrl);
                    if (length != null) {
                        asset.setVideoDurationSeconds(Math.round(length.floatValue()));
                    }
                    asset.setStatus(FbAdsAsset.AssetStatus.READY);
                    asset.setUpdatedAt(LocalDateTime.now());

                    assetRepo.save(asset);
                    logger.info("Cronjob: Video {} đã xử lý xong và chuyển thành READY!", asset.getFbAssetId());
                } else {
                    logger.debug("Video {} vẫn đang được FB xử lý, sẽ quay lại sau.", asset.getFbAssetId());
                }

            } catch (Exception e) {
                logger.error("Cronjob lỗi khi kiểm tra video {}: {}", asset.getFbAssetId(), e.getMessage());
            }
        }
    }
}
