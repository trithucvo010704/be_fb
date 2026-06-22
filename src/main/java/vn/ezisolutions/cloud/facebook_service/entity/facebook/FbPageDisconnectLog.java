package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_page_disconnect_logs", indexes = {
        @Index(name = "idx_fb_page_disconnect_logs_page_id", columnList = "page_id"),
        @Index(name = "idx_fb_page_disconnect_logs_disconnected_by", columnList = "disconnected_by"),
        @Index(name = "idx_fb_page_disconnect_logs_disconnected_at", columnList = "disconnected_at")
})
public class FbPageDisconnectLog {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "page_id", nullable = false)
    private UUID pageId;

    @Column(name = "disconnected_by", length = 50)
    private String disconnectedBy;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_token_status", length = 32)
    private FbPage.TokenStatus previousTokenStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_token_status", nullable = false, length = 32)
    @Builder.Default
    private FbPage.TokenStatus newTokenStatus = FbPage.TokenStatus.DISCONNECTED;

    @Builder.Default
    @Column(name = "token_disabled", nullable = false)
    private Boolean tokenDisabled = true;

    @Builder.Default
    @Column(name = "data_hidden_from_ui", nullable = false)
    private Boolean dataHiddenFromUi = true;

    @Column(name = "disconnected_at", nullable = false)
    private LocalDateTime disconnectedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
