package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbAdsAccountRepository extends JpaRepository<FbAdsAccount, UUID> {
    Optional<FbAdsAccount> findByAccountId(String accountId);

    List<FbAdsAccount> findByOwnerIdAndFbAccountStatus(String ownerId, Integer fbAccountStatus);
}
