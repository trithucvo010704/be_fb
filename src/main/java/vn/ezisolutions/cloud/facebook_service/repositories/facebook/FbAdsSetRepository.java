package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsSet;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbAdsSetRepository extends JpaRepository<FbAdsSet, UUID> {
    Optional<FbAdsSet> findByFbAdSetId(String fbAdSetId);
}
