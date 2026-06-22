package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePost;

import java.util.UUID;

@Repository
public interface FbPagePostRepository extends JpaRepository<FbPagePost, UUID> {
    Page<FbPagePost> findByPageIdOrderByCreatedAtDesc(UUID pageId, Pageable pageable);
}
