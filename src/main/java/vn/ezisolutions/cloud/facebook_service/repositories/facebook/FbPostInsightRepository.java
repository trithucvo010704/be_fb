package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostInsight;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbPostInsightRepository extends JpaRepository<FbPostInsight, UUID> {
    Optional<FbPostInsight> findByPostId(UUID postId);
}
