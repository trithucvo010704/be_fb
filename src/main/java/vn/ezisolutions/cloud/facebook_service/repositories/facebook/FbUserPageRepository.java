package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUserPage;

import java.util.List;
import java.util.UUID;

@Repository
public interface FbUserPageRepository extends JpaRepository<FbUserPage, UUID> {
    List<FbUserPage> findByFbUserId(String fbUserId);

    List<FbUserPage> findByFbPageId(String fbPageId);

    boolean existsByFbUserIdAndFbPageId(String fbUserId, String fbPageId);

    void deleteByFbUserIdAndFbPageId(String fbUserId, String fbPageId);
}
