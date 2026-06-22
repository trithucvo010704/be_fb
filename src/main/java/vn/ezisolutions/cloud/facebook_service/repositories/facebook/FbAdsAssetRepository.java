package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsAsset;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbAdsAssetRepository extends JpaRepository<FbAdsAsset, UUID> {
    Optional<FbAdsAsset> findByAdAccountIdAndHashAndType(String adAccountId, String hash, FbAdsAsset.AssetType type);

    Optional<FbAdsAsset> findByAdAccountIdAndUrlAndType(String adAccountId, String url, FbAdsAsset.AssetType type);

    List<FbAdsAsset> findByStatusAndType(FbAdsAsset.AssetStatus status, FbAdsAsset.AssetType type);
}
