package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import vn.ezisolutions.cloud.facebook_service.enums.PlanStatus;
import vn.ezisolutions.cloud.facebook_service.enums.PlanStep;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_ads_plans", indexes = {
        @Index(name = "idx_fb_ads_plans_owner", columnList = "owner_id")
})
public class FbAdsPlan {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "plan_id", unique = true, nullable = false, length = 50)
    private String planId;

    @Column(name = "owner_id", nullable = false, length = 50)
    private String ownerId;

    @Column(name = "ad_account_id", length = 50)
    private String adAccountId;

    @Column(name = "page_id", length = 50)
    private String pageId;

    @Column(name = "campaign_id", length = 50)
    private String campaignId;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", length = 20)
    private PlanStep currentStep;

    @Column(name = "total_ads")
    private Integer totalAds;

    @Column(name = "success_count")
    private Integer successCount;

    @Column(name = "failure_count")
    private Integer failureCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private PlanStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

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
}
