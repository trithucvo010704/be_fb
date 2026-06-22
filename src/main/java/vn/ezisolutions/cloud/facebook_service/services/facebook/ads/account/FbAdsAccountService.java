package vn.ezisolutions.cloud.facebook_service.services.facebook.ads.account;

import com.facebook.ads.sdk.APINodeList;
import com.facebook.ads.sdk.AdAccount;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbAdsAccountRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbUserRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FbAdsAccountService {
    private static final Logger log = LoggerFactory.getLogger(FbAdsAccountService.class);

    private final FbAdsAccountRepository accountRepo;
    private final FbUserRepository fbUserRepository;
    private final FbAdsAccountClientService clientService;
    private final FacebookTokenService facebookTokenService;

    @Transactional(rollbackFor = Exception.class)
    public List<FbAdsAccount> syncAdAccounts(String ownerId, String fbUserId) throws CustomException {
        log.info("Start syncAdAccounts - ownerId: {}, fbUserId: {}", ownerId, fbUserId);

        String token = facebookTokenService.getUserAccessToken(fbUserId);
        if (token == null) {
            throw new CustomException(401, "Facebook Token không hợp lệ. Vui lòng kết nối lại!");
        }

        FbUser fbUser = fbUserRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new CustomException(404, "Tài khoản hệ thống chưa liên kết với Facebook."));

        if (!fbUser.getFbUserId().equals(fbUserId)) {
            log.warn("Security Alert: System User {} tried to sync unauthorized FB User {}", ownerId, fbUserId);
            throw new CustomException(403, "Bạn không có quyền thao tác trên tài khoản Facebook này.");
        }

        APINodeList<AdAccount> currentPage = clientService.fetchAdAccountsFromFacebook(fbUserId, token);
        List<FbAdsAccount> accountsToSave = new ArrayList<>();

        if (currentPage == null || currentPage.isEmpty()) {
            log.info("User {} has no ad accounts.", fbUserId);
            return accountsToSave;
        }

        while (currentPage != null) {
            for (AdAccount fbAcc : currentPage) {
                if (fbAcc.getId() == null)
                    continue;

                FbAdsAccount account = mapAndSyncAdAccount(fbAcc, fbUserId, ownerId);
                accountsToSave.add(account);
            }
            try {
                currentPage = currentPage.nextPage();
            } catch (Exception e) {
                log.error("Error fetching next page of ad accounts from Facebook API", e);
                currentPage = null;
            }
        }

        List<FbAdsAccount> savedAccounts = accountRepo.saveAll(accountsToSave);
        log.info("Successfully synced {} ad accounts.", savedAccounts.size());
        return savedAccounts;
    }

    private FbAdsAccount mapAndSyncAdAccount(AdAccount fbAcc, String fbUserId, String ownerId) {
        FbAdsAccount account = accountRepo.findByAccountId(fbAcc.getId())
                .orElse(FbAdsAccount.builder()
                        .accountId(fbAcc.getId())
                        .fbUserId(fbUserId)
                        .ownerId(ownerId)
                        .createdAt(LocalDateTime.now())
                        .build());

        account.setName(fbAcc.getFieldName() != null ? fbAcc.getFieldName() : "Unnamed Account");
        account.setCurrency(fbAcc.getFieldCurrency() != null ? fbAcc.getFieldCurrency() : "VND");
        account.setTimezoneName(
                fbAcc.getFieldTimezoneName() != null ? fbAcc.getFieldTimezoneName() : "Asia/Ho_Chi_Minh");

        account.setFbAccountStatus(parseAccountStatus(fbAcc.getId(), fbAcc.getFieldAccountStatus()));
        account.setBalance(parseBalance(fbAcc.getId(), fbAcc.getFieldBalance()));

        account.setSyncStatus(FbAdsAccount.SyncStatus.SUCCESS);
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private int parseAccountStatus(String id, Object statusField) {
        if (statusField != null) {
            try {
                return Integer.parseInt(statusField.toString());
            } catch (NumberFormatException e) {
                log.warn("Error parsing Account Status for ID {}: {}", id, statusField);
            }
        }
        return 0;
    }

    private BigDecimal parseBalance(String id, String balanceField) {
        if (balanceField != null) {
            try {
                return new BigDecimal(balanceField);
            } catch (NumberFormatException e) {
                log.warn("Error parsing Balance for ID {}: {}", id, balanceField);
            }
        }
        return BigDecimal.ZERO;
    }

    public List<FbAdsAccount> getActiveAdAccounts(String ownerId) {
        return accountRepo.findByOwnerIdAndFbAccountStatus(ownerId, 1);
    }

    public FbAdsAccount validateAndGetAccount(String adAccountId, String ownerId) throws CustomException {
        FbAdsAccount account = accountRepo.findByAccountId(adAccountId)
                .orElseThrow(() -> new CustomException(404, "Không tìm thấy tài khoản quảng cáo."));

        if (!ownerId.equals(account.getOwnerId())) {
            throw new CustomException(403, "Bạn không có quyền thao tác trên tài khoản này.");
        }
        if (account.getFbAccountStatus() != 1) {
            throw new CustomException(403, "Tài khoản quảng cáo đang bị khóa hoặc không hoạt động.");
        }

        return account;
    }
}
