package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_users", indexes = {
        @Index(name = "idx_fb_users_owner_user_id", columnList = "owner_user_id"),
        @Index(name = "idx_fb_users_status", columnList = "token_status")
})
public class FbUser {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "owner_user_id", length = 50)
    private String ownerUserId;

    @Column(name = "fb_user_id", unique = true, nullable = false, length = 100)
    private String fbUserId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "access_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String accessTokenEncrypted;

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    @Column(name = "data_access_expires_at")
    private Instant dataAccessExpiresAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "granted_permissions", columnDefinition = "JSON")
    private List<String> grantedPermissions;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_status", nullable = false, length = 20)
    private TokenStatus tokenStatus;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public enum TokenStatus {
        ACTIVE, DISCONNECTED, EXPIRED, REVOKED, INVALID, MISSING_PERMISSION
    }

    /**
     * Backward-compatible alias while old phase-later services are dormant.
     */
    public String getOwnerId() {
        return ownerUserId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerUserId = ownerId;
    }

    public String getAccessToken() {
        return accessTokenEncrypted;
    }

    public void setAccessToken(String accessToken) {
        this.accessTokenEncrypted = accessToken;
    }

    public Instant getTokenExpiredAt() {
        return tokenExpiresAt;
    }

    public void setTokenExpiredAt(Instant tokenExpiredAt) {
        this.tokenExpiresAt = tokenExpiredAt;
    }
}
