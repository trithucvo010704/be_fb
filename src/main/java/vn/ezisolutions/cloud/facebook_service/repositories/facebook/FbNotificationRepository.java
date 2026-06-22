package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbNotification;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbNotificationRepository extends JpaRepository<FbNotification, UUID> {
    Optional<FbNotification> findByEventId(String eventId);
}
