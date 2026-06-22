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
@Table(name = "fb_ads_sets", indexes = {
    @Index(name = "idx_fb_ads_sets_campaign", columnList = "campaign_id"),
    @Index(name = "idx_fb_ads_sets_acc", columnList = "ad_account_id"),
    @Index(name = "idx_fb_ads_sets_plan", columnList = "plan_id")
})
public class FbAdsSet {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_adset_id", unique = true, length = 50)
    private String fbAdSetId;

    @Column(name = "campaign_id", nullable = false, length = 50)
    private String campaignId;

    @Column(name = "ad_account_id", length = 50)
    private String adAccountId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "plan_id", length = 50)
    private String planId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "effective_status", length = 50)
    private String effectiveStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "daily_budget")
    private Long dailyBudget;

    @Column(name = "lifetime_budget")
    private Long lifetimeBudget;

    @Column(name = "bid_strategy", length = 50)
    private String bidStrategy;

    @Column(name = "bid_amount")
    private Long bidAmount;

    @Column(name = "billing_event", length = 50)
    private String billingEvent;

    @Column(name = "optimization_goal", length = 50)
    private String optimizationGoal;

    @Column(name = "targeting", columnDefinition = "TEXT")
    private String targetingJson;

    @Column(name = "promoted_object", columnDefinition = "TEXT")
    private String promotedObjectJson;

    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

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
