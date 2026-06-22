package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.adset;

import com.facebook.ads.sdk.AdSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FbCreateAdSetRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsSet;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsSetRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account.FbAdsAccountService;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class FbAdsSetService {
    private static final Logger logger = LoggerFactory.getLogger(FbAdsSetService.class);
    private final FbAdsSetClientService clientService;
    private final FacebookTokenService tokenService;
    private final FbAdsAccountService accountService;
    private final FbAdsSetRepository adSetRepository;
    private final ObjectMapper objectMapper;

    public String createAdSet(String ownerId, FbCreateAdSetRequest request) throws CustomException {
        logger.info("createAdSet start - adAccountId: {}, ownerId: {}", request.getAdAccountId(), ownerId);
        FbAdsAccount account = accountService.validateAndGetAccount(request.getAdAccountId(), ownerId);
        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null)
            throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        try {
            AdSet fbAdSet = clientService.createAdSet(request.getAdAccountId(), token, request);
            LocalDateTime parsedStartTime = parseFacebookTime(request.getStartTime());
            LocalDateTime parsedEndTime = parseFacebookTime(request.getEndTime());
            FbAdsSet newAdSet = FbAdsSet.builder()
                    .fbAdSetId(fbAdSet.getId())
                    .campaignId(request.getCampaignId())
                    .adAccountId(request.getAdAccountId())
                    .ownerId(ownerId)
                    .planId(request.getPlanId())
                    .name(request.getName())
                    .optimizationGoal(request.getOptimizationGoal())
                    .billingEvent(request.getBillingEvent())
                    .status("PAUSED")
                    .effectiveStatus("PAUSED")
                    .dailyBudget(request.getDailyBudget())
                    .lifetimeBudget(request.getLifetimeBudget())
                    .bidStrategy(request.getBidStrategy())
                    .bidAmount(request.getBidAmount())
                    .startTime(parsedStartTime)
                    .endTime(parsedEndTime)
                    .targetingJson(objectMapper.writeValueAsString(request.getTargeting()))
                    .promotedObjectJson(request.getPromotedObject() != null
                            ? objectMapper.writeValueAsString(request.getPromotedObject())
                            : null)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            adSetRepository.save(newAdSet);
            logger.info("Tạo thành công AdSet ID trên FB và lưu DB: {}", fbAdSet.getId());
            return fbAdSet.getId();

        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi tạo AdSet: {}", e.getMessage());
            throw new CustomException(400, "Không thể tạo AdSet trên Facebook: " + e.getMessage());
        }
    }

    public void deleteAdSetOnFacebook(String ownerId, String adAccountId, String adSetId) throws CustomException {
        logger.info("deleteAdSetOnFacebook start - adSetId: {}, adAccountId: {}, ownerId: {}", adSetId, adAccountId, ownerId);
        FbAdsAccount account = accountService.validateAndGetAccount(adAccountId, ownerId);
        String token = tokenService.getUserAccessToken(account.getFbUserId());
        if (token == null)
            throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");

        try {
            clientService.deleteAdSet(adSetId, token);
            adSetRepository.findByFbAdSetId(adSetId).ifPresent(adSet -> {
                adSet.setStatus("DELETED");
                adSet.setEffectiveStatus("DELETED");
                adSet.setUpdatedAt(LocalDateTime.now());
                adSetRepository.save(adSet);
            });
            logger.info("Đã dọn dẹp và cập nhật DELETED cho AdSet: {}", adSetId);
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Lỗi khi xóa AdSet mồ côi: {}", e.getMessage());
            throw new CustomException(500, "Lỗi xóa AdSet mồ côi: " + e.getMessage());
        }
    }

    private LocalDateTime parseFacebookTime(String timeString) {
        if (timeString == null || timeString.isEmpty())
            return null;
        try {
            return OffsetDateTime.parse(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
                    .toLocalDateTime();
        } catch (Exception e) {
            logger.warn("Không thể parse thời gian Facebook: {}. Lỗi: {}", timeString, e.getMessage());
            return null;
        }
    }
}
