package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePostMedia;

import java.util.List;
import java.util.UUID;

@Repository
public interface FbPagePostMediaRepository extends JpaRepository<FbPagePostMedia, UUID> {
    List<FbPagePostMedia> findByPostIdOrderByMediaOrderAsc(UUID postId);
}
