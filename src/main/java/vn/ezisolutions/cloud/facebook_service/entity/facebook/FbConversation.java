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
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_conversations", indexes = {
        @Index(name = "idx_fb_conversations_page_id", columnList = "page_id"),
        @Index(name = "idx_fb_conversations_page_time", columnList = "page_id,last_message_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_fb_conversations_page_sender", columnNames = {"page_id", "sender_psid"})
})
public class FbConversation {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "sender_psid", nullable = false, length = 100)
    private String senderPsid;

    @Column(name = "fb_conversation_id", length = 150)
    private String fbConversationId;

    @Column(name = "sender_name", length = 255)
    private String senderName;

    @Column(name = "sender_avatar_url", length = 2048)
    private String senderAvatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private Status status = Status.OPEN;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_inbound_at")
    private LocalDateTime lastInboundAt;

    @Column(name = "last_outbound_at")
    private LocalDateTime lastOutboundAt;

    @Builder.Default
    @Column(name = "unread_count", nullable = false)
    private Integer unreadCount = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_profile", columnDefinition = "JSON")
    private Map<String, Object> rawProfile;

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
        OPEN, IN_PROGRESS, RESOLVED, ARCHIVED
    }
}
