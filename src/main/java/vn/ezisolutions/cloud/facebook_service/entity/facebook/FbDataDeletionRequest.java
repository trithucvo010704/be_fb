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
@Table(name = "fb_data_deletion_requests", indexes = {
        @Index(name = "idx_fb_data_deletion_requests_requester_email", columnList = "requester_email"),
        @Index(name = "idx_fb_data_deletion_requests_fb_user_id", columnList = "fb_user_id"),
        @Index(name = "idx_fb_data_deletion_requests_fb_page_id", columnList = "fb_page_id"),
        @Index(name = "idx_fb_data_deletion_requests_status", columnList = "status"),
        @Index(name = "idx_fb_data_deletion_requests_requested_at", columnList = "requested_at")
})
public class FbDataDeletionRequest {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "requester_email", length = 255)
    private String requesterEmail;

    @Column(name = "fb_user_id", length = 100)
    private String fbUserId;

    @Column(name = "fb_page_id", length = 100)
    private String fbPageId;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type", nullable = false, length = 50)
    @Builder.Default
    private RequestType requestType = RequestType.DELETE_FACEBOOK_DATA;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    @Builder.Default
    private Status status = Status.RECEIVED;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

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

    public enum RequestType {
        DISCONNECT_PAGE, DELETE_FACEBOOK_DATA
    }

    public enum Status {
        RECEIVED, PROCESSING, COMPLETED, REJECTED
    }
}
