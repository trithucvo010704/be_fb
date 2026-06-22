package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.ad;

import com.facebook.ads.sdk.Ad;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAds;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FbAdsService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsService.class);
    private final FbAdsClientService clientService;
    private final FacebookTokenService tokenService;
    private final FbAdsAccountService accountService;
    private final FbAdsRepository adsRepository;

    public String createAd(String ownerId, FbCreateAdRequest request) throws CustomException {
        logger.info("createAd start - adAccountId: {}, ownerId: {}", request.getAdAccountId(), ownerId);
        FbAdsAccount account = accountService.validateAndGetAccount(request.getAdAccountId(), ownerId);
        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null) throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        try {
            Ad fbAd = clientService.createAdOnFacebook(request.getAdAccountId(), token, request);

            FbAds newAd = FbAds.builder()
                    .fbAdId(fbAd.getId())
                    .adAccountId(request.getAdAccountId())
                    .adsetId(request.getAdSetId())
                    .creativeId(request.getCreativeId())
                    .planId(request.getPlanId())
                    .ownerId(ownerId)
                    .name(request.getName())
                    .status(fbAd.getFieldStatus().toString())
                    .effectiveStatus(fbAd.getFieldEffectiveStatus() != null ? fbAd.getFieldEffectiveStatus().toString() : null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            adsRepository.save(newAd);
            logger.info("Tạo Ad thành công trên Facebook với ID: {}", fbAd.getId());
            return fbAd.getId();

        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo Ad cho tài khoản {}: {}", request.getAdAccountId(), e.getMessage());
            throw new CustomException(400, "Không thể tạo Quảng cáo trên Facebook: " + e.getMessage());
        }
    }
}
