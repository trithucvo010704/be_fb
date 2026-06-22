package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbPageRepository extends JpaRepository<FbPage, UUID> {
    Optional<FbPage> findByFbPageId(String fbPageId);

    @Query("SELECT p FROM FbPage p WHERE p.fbPageId IN (SELECT up.fbPageId FROM FbUserPage up WHERE up.fbUserId = :fbUserId)")
    List<FbPage> findAllByFbUserIdsContaining(@Param("fbUserId") String fbUserId);

    List<FbPage> findByFbPageIdIn(List<String> fbPageIds);

    List<FbPage> findByConnectionStatusOrderByUpdatedAtDesc(FbPage.ConnectionStatus connectionStatus);
}
