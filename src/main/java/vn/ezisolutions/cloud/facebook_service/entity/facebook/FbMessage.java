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
@Table(name = "fb_messages", indexes = {
        @Index(name = "idx_fb_messages_conversation_id", columnList = "conversation_id"),
        @Index(name = "idx_fb_messages_page_id", columnList = "page_id"),
        @Index(name = "idx_fb_messages_facebook_message_id", columnList = "facebook_message_id"),
        @Index(name = "idx_fb_messages_page_time", columnList = "page_id,occurred_at"),
        @Index(name = "idx_fb_messages_conversation_time", columnList = "conversation_id,occurred_at")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_fb_messages_event_id", columnNames = {"event_id"})
})
public class FbMessage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "event_id", unique = true, length = 150)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, length = 30)
    private Direction direction;

    @Column(name = "facebook_message_id", length = 150)
    private String facebookMessageId;

    @Column(name = "sender_id", length = 100)
    private String senderId;

    @Column(name = "recipient_id", length = 100)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 30)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "attachment_url", length = 2048)
    private String attachmentUrl;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_payload", columnDefinition = "JSON")
    private Map<String, Object> rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private Status status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

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

    public enum Direction {
        INBOUND, OUTBOUND
    }

    public enum MessageType {
        TEXT, IMAGE, FILE, UNKNOWN
    }

    public enum Status {
        RECEIVED, SENT, FAILED
    }
}
