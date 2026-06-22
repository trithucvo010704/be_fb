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
@Table(name = "fb_ads_campaigns", indexes = {
        @Index(name = "idx_fb_ads_campaigns_acc", columnList = "ad_account_id"),
        @Index(name = "idx_fb_ads_campaigns_plan", columnList = "plan_id"),
        @Index(name = "idx_fb_ads_campaigns_owner", columnList = "owner_id")
})
public class FbAdsCampaign {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "campaign_id", nullable = false, length = 50)
    private String campaignId;

    @Column(name = "fb_campaign_id", unique = true, length = 50)
    private String fbCampaignId;

    @Column(name = "ad_account_id", nullable = false, length = 50)
    private String adAccountId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "plan_id", length = 50)
    private String planId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "objective", length = 50)
    private String objective;

    @Column(name = "buying_type", length = 50)
    private String buyingType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "special_ad_categories", columnDefinition = "JSON")
    @Builder.Default
    private List<String> specialAdCategories = List.of("NONE");

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "special_ad_category_countries", columnDefinition = "JSON")
    private List<String> specialAdCategoryCountries;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "effective_status", length = 50)
    private String effectiveStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "is_cbo")
    private Boolean isCbo;

    @Column(name = "daily_budget")
    private Long dailyBudget;

    @Column(name = "lifetime_budget")
    private Long lifetimeBudget;

    @Column(name = "spending_limit")
    private Long spendingLimit;

    @Column(name = "bid_strategy", length = 50)
    private String bidStrategy;

    @Column(name = "start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Column(name = "stop_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime stopTime;

    @Column(name = "fb_created_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fbCreatedTime;

    @Column(name = "fb_updated_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fbUpdatedTime;

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
