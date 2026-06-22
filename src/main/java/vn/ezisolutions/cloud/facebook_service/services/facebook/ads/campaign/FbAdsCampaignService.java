package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.campaign;

import com.facebook.ads.sdk.Campaign;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateCampaignRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsCampaign;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsCampaignRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FbAdsCampaignService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsCampaignService.class);
    private final FbAdsCampaignClientService clientService;
    private final FacebookTokenService tokenService;
    private final FbAdsAccountService accountService;
    private final FbAdsCampaignRepository campaignRepository;

    public String createCampaign(String ownerId, FbCreateCampaignRequest request) throws CustomException {
        logger.info("createCampaign start - adAccountId: {}, ownerId: {}", request.getAdAccountId(), ownerId);
        FbAdsAccount account = accountService.validateAndGetAccount(request.getAdAccountId(), ownerId);

        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null)
            throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        try {
            String buyingType = request.getBuyingType();
            if (buyingType == null || buyingType.isBlank()) {
                buyingType = "AUCTION";
            }

            Campaign fbCampaign = clientService.createCampaignOnFacebook(request.getAdAccountId(), token, request);
            FbAdsCampaign newCampaign = FbAdsCampaign.builder()
                    .fbCampaignId(fbCampaign.getId())
                    .adAccountId(request.getAdAccountId())
                    .ownerId(ownerId)
                    .name(request.getName())
                    .objective(request.getObjective().toString())
                    .buyingType(buyingType)
                    .specialAdCategories(
                            request.getSpecialAdCategories() != null && !request.getSpecialAdCategories().isEmpty()
                                     ? request.getSpecialAdCategories()
                                     : List.of("NONE"))
                    .status("PAUSED")
                    .effectiveStatus("PAUSED")
                    .isCbo(request.getIsCbo())
                    .dailyBudget(request.getDailyBudget())
                    .bidStrategy(request.getBidStrategy() != null ? request.getBidStrategy().name() : null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            campaignRepository.save(newCampaign);
            logger.info("Tạo Campaign thành công trên Facebook với ID: {}", fbCampaign.getId());
            return fbCampaign.getId();

        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi khi tạo Campaign cho tài khoản {}: {}", request.getAdAccountId(), e.getMessage());
            throw new CustomException(400, "Không thể tạo Chiến dịch trên Facebook: " + e.getMessage());
        }
    }
}
