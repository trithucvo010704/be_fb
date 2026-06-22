package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface FbConversationRepository extends JpaRepository<FbConversation, UUID> {
    Optional<FbConversation> findByPageIdAndSenderPsid(UUID pageId, String senderPsid);

    Page<FbConversation> findByPageIdOrderByLastMessageAtDesc(UUID pageId, Pageable pageable);
}
