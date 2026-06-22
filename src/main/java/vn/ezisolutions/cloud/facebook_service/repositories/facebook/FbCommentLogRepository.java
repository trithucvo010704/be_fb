package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbCommentLog;

import java.util.List;
import java.util.UUID;

@Repository
public interface FbCommentLogRepository extends JpaRepository<FbCommentLog, UUID> {
    List<FbCommentLog> findByCommentIdOrderByCreatedAtDesc(String commentId);
}
