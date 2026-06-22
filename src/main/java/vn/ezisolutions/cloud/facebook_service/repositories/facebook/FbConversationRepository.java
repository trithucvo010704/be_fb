package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbConversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbConversationRepository extends JpaRepository<FbConversation, UUID> {
    Optional<FbConversation> findByPageIdAndSenderPsid(UUID pageId, String senderPsid);

    List<FbConversation> findByPageIdOrderByLastMessageAtDesc(UUID pageId);
}
