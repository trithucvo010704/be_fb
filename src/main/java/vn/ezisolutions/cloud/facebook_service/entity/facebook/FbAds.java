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
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_ads", indexes = {
    @Index(name = "idx_fb_ads_acc", columnList = "ad_account_id"),
    @Index(name = "idx_fb_ads_campaign", columnList = "campaign_id"),
    @Index(name = "idx_fb_ads_adset", columnList = "adset_id"),
    @Index(name = "idx_fb_ads_plan", columnList = "plan_id")
})
public class FbAds {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_ad_id", unique = true, length = 50)
    private String fbAdId;

    @Column(name = "ad_account_id", nullable = false, length = 50)
    private String adAccountId;

    @Column(name = "campaign_id", length = 50)
    private String campaignId;

    @Column(name = "adset_id", nullable = false, length = 50)
    private String adsetId;

    @Column(name = "creative_id", nullable = false, length = 50)
    private String creativeId;

    @Column(name = "plan_id", length = 50)
    private String planId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "effective_status", length = 50)
    private String effectiveStatus;

    @Column(name = "source_post_id", length = 50)
    private String sourcePostId;

    @Column(name = "preview_shareable_link", length = 512)
    private String previewShareableLink;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tracking_specs", columnDefinition = "JSON")
    private List<Map<String, Object>> trackingSpecs;

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
