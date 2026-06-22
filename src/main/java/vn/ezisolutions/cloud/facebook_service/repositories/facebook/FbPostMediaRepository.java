package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;

import java.util.List;
import java.util.UUID;

@Repository
public interface FbPostMediaRepository extends JpaRepository<FbPostMedia, UUID> {
    List<FbPostMedia> findByPostIdOrderByMediaOrderAsc(UUID postId);

    List<FbPostMedia> findByPostId(UUID postId);

    List<FbPostMedia> findByPostIdInOrderByMediaOrderAsc(List<UUID> postIds);
}
