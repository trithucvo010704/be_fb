package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbMessage;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbMessageRepository extends JpaRepository<FbMessage, UUID> {
    Page<FbMessage> findByConversationIdOrderByOccurredAtAsc(UUID conversationId, Pageable pageable);

    Page<FbMessage> findByConversationIdAndPageIdOrderByOccurredAtAsc(UUID conversationId, UUID pageId, Pageable pageable);

    Optional<FbMessage> findByEventId(String eventId);
}
