package vn.ezisolutions.cloud.facebook_service.repositories.facebook;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbUser;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FbUserRepository extends JpaRepository<FbUser, UUID> {
    Optional<FbUser> findByFbUserId(String fbUserId);

    Optional<FbUser> findByOwnerUserId(String ownerUserId);

    boolean existsByOwnerUserId(String ownerUserId);

    @Query("SELECT u FROM FbUser u WHERE u.ownerUserId = :ownerId")
    Optional<FbUser> findByOwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT COUNT(u) > 0 FROM FbUser u WHERE u.ownerUserId = :ownerId")
    boolean existsByOwnerId(@Param("ownerId") String ownerId);
}
