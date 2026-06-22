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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_pages", indexes = {
    @Index(name = "idx_fb_pages_connected_by_fb_user_id", columnList = "connected_by_fb_user_id"),
    @Index(name = "idx_fb_pages_connected_by_user_id", columnList = "connected_by_user_id"),
    @Index(name = "idx_fb_pages_status", columnList = "token_status"),
    @Index(name = "idx_fb_pages_connection_status", columnList = "connection_status"),
    @Index(name = "idx_fb_pages_webhook_subscribed", columnList = "webhook_subscribed")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_fb_pages_fb_page_id", columnNames = {"fb_page_id"})
})
public class FbPage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_page_id", nullable = false, length = 100)
    private String fbPageId;

    @Column(name = "page_name", nullable = false, length = 255)
    private String pageName;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "category", length = 255)
    private String category;

    @Column(name = "connected_by_fb_user_id", nullable = false)
    private UUID connectedByFbUserId;

    @Column(name = "connected_by_user_id", length = 50)
    private String connectedByUserId;

    @Column(name = "page_access_token_encrypted", nullable = false, columnDefinition = "TEXT")
    private String pageAccessTokenEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_status", nullable = false, length = 32)
    @Builder.Default
    private TokenStatus tokenStatus = TokenStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false, length = 32)
    @Builder.Default
    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

    @Builder.Default
    @Column(name = "webhook_subscribed", nullable = false)
    private Boolean webhookSubscribed = false;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "granted_permissions", columnDefinition = "JSON")
    private List<String> grantedPermissions;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "missing_permissions", columnDefinition = "JSON")
    private List<String> missingPermissions;

    @Column(name = "webhook_subscribed_at")
    private LocalDateTime webhookSubscribedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    @Column(name = "disconnected_at")
    private LocalDateTime disconnectedAt;

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

    public enum ConnectionStatus {
        CONNECTED, DISCONNECTED, MISSING_PERMISSION
    }

    public String getPageAccessToken() {
        return pageAccessTokenEncrypted;
    }

    public void setPageAccessToken(String pageAccessToken) {
        this.pageAccessTokenEncrypted = pageAccessToken;
    }

    public List<String> getPagePermissions() {
        return grantedPermissions;
    }

    public void setPagePermissions(List<String> pagePermissions) {
        this.grantedPermissions = pagePermissions;
    }

    public String getProjectId() {
        return connectedByUserId;
    }

    public void setProjectId(String projectId) {
        this.connectedByUserId = projectId;
    }
}
