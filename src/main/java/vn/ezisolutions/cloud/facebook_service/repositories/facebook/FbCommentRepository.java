package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbComment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbCommentRepository extends JpaRepository<FbComment, UUID> {
    Optional<FbComment> findByFbCommentId(String fbCommentId);

    boolean existsByFbCommentId(String fbCommentId);

    List<FbComment> findByPostId(UUID postId);
}
