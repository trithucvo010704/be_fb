package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbPageClientRepository extends JpaRepository<FbPageClient, UUID> {
    Optional<FbPageClient> findByPageIdAndClientId(UUID pageId, String clientId);

    List<FbPageClient> findByStatus(FbPageClient.Status status);
}
