package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_page_clients", indexes = {
        @Index(name = "idx_fb_page_clients_page_id", columnList = "page_id"),
        @Index(name = "idx_fb_page_clients_client_id", columnList = "client_id"),
        @Index(name = "idx_fb_page_clients_connected_by_user_id", columnList = "connected_by_user_id"),
        @Index(name = "idx_fb_page_clients_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_fb_page_clients_page_client", columnNames = {"page_id", "client_id"})
})
public class FbPageClient {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Builder.Default
    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId = "central_review";

    @Column(name = "connected_by_fb_user_id", nullable = false)
    private UUID connectedByFbUserId;

    @Column(name = "connected_by_user_id", length = 50)
    private String connectedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private Status status = Status.CONNECTED;

    @Builder.Default
    @Column(name = "message_enabled", nullable = false)
    private Boolean messageEnabled = true;

    @Builder.Default
    @Column(name = "post_enabled", nullable = false)
    private Boolean postEnabled = true;

    @Column(name = "connected_at", nullable = false)
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

    public enum Status {
        CONNECTED, DISCONNECTED, MISSING_PERMISSION
    }
}
