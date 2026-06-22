package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_ads_accounts", indexes = {
        @Index(name = "idx_fb_ads_accounts_fb_user", columnList = "fb_user_id"),
        @Index(name = "idx_fb_ads_accounts_owner", columnList = "owner_id")
})
public class FbAdsAccount {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_user_id", length = 50)
    private String fbUserId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "account_id", unique = true, nullable = false, length = 50)
    private String accountId;

    @Column(name = "business_id", length = 50)
    private String businessId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "timezone_name", length = 100)
    private String timezoneName;

    @Column(name = "fb_account_status")
    private Integer fbAccountStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    @Builder.Default
    private SyncStatus syncStatus = SyncStatus.PENDING;

    @Column(name = "disable_reason", columnDefinition = "TEXT")
    private String disableReason;

    @Column(name = "balance", precision = 18, scale = 2)
    private BigDecimal balance;

    @Column(name = "amount_spent", precision = 18, scale = 2)
    private BigDecimal amountSpent;

    @Column(name = "credit_limit", precision = 18, scale = 2)
    private BigDecimal creditLimit;

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

    public enum SyncStatus {
        PENDING,
        PROCESSING,
        SUCCESS,
        FAILED
    }
}
