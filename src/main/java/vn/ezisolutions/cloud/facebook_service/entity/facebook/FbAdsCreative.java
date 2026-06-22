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
@Table(name = "fb_ads_creatives", indexes = {
    @Index(name = "idx_fb_ads_creatives_acc", columnList = "ad_account_id"),
    @Index(name = "idx_fb_ads_creatives_page", columnList = "page_id"),
    @Index(name = "idx_fb_ads_creatives_plan", columnList = "plan_id")
})
public class FbAdsCreative {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_creative_id", unique = true, length = 50)
    private String fbCreativeId;

    @Column(name = "ad_account_id", length = 50)
    private String adAccountId;

    @Column(name = "page_id", length = 50)
    private String pageId;

    @Column(name = "plan_id", length = 50)
    private String planId;

    @Column(name = "asset_id", length = 50)
    private String assetId;

    @Column(name = "owner_id", length = 50)
    private String ownerId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private FbCreativeType type;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "source_post_id", length = 50)
    private String sourcePostId;

    @Column(name = "object_story_id", length = 50)
    private String objectStoryId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "headline", length = 255)
    private String headline;

    @Column(name = "link_url", length = 512)
    private String linkUrl;

    @Column(name = "call_to_action_type", length = 50)
    private String callToActionType;

    @Column(name = "image_hash", length = 100)
    private String imageHash;

    @Column(name = "video_id", length = 50)
    private String videoId;

    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;

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

    public enum FbCreativeType {
        EXISTING_POST,
        MEDIA_IMAGE,
        MEDIA_VIDEO,
        CAROUSEL
    }
}
