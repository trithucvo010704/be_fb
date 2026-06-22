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
@Table(name = "fb_ads_assets", indexes = {
    @Index(name = "idx_fb_ads_assets_acc", columnList = "ad_account_id"),
    @Index(name = "idx_fb_ads_assets_plan", columnList = "plan_id"),
    @Index(name = "idx_fb_ads_assets_hash", columnList = "hash")
})
public class FbAdsAsset {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "ad_account_id", length = 50)
    private String adAccountId;

    @Column(name = "plan_id", length = 50)
    private String planId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "fb_asset_id", length = 50)
    private String fbAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private AssetType type;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "url", nullable = false, length = 512)
    private String url;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

    @Column(name = "hash", length = 100)
    private String hash;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "video_duration_seconds")
    private Integer videoDurationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AssetStatus status;

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

    public enum AssetType {
        IMAGE,
        VIDEO
    }

    public enum AssetStatus {
        UPLOADING,
        PROCESSING,
        READY,
        FAILED,
        EXPIRED
    }
}
