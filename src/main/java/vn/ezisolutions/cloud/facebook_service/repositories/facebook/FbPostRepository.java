package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbPostRepository extends JpaRepository<FbPost, UUID> {
    List<FbPost> findByStatus(FbPost.PostStatus status);

    @Query("SELECT p FROM FbPost p WHERE p.status = :status AND p.scheduledTime <= :now")
    List<FbPost> findPendingPostsToPublish(@Param("status") FbPost.PostStatus status, @Param("now") LocalDateTime now);

    Optional<FbPost> findByFbPostId(String id);

    Page<FbPost> findAllByPageIdOrderByCreatedAtDesc(String pageId, Pageable pageable);
}
