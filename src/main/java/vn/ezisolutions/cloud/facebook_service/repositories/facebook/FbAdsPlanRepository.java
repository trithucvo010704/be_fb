package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbAdsPlan;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbAdsPlanRepository extends JpaRepository<FbAdsPlan, UUID> {
    Optional<FbAdsPlan> findByPlanId(String planId);
}
