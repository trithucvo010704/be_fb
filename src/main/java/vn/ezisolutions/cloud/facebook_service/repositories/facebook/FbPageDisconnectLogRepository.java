package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPageDisconnectLog;

import java.util.UUID;

@Repository
public interface FbPageDisconnectLogRepository extends JpaRepository<FbPageDisconnectLog, UUID> {
}
