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
@Table(name = "fb_post_insights", indexes = {
    @Index(name = "idx_fb_post_insights_page", columnList = "page_id")
})
public class FbPostInsight {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "post_id", unique = true, nullable = false)
    private UUID postId;

    @Column(name = "page_id", nullable = false, length = 50)
    private String pageId;

    @Column(name = "total_reach")
    private Integer totalReach;

    @Column(name = "organic_reach")
    private Integer organicReach;

    @Column(name = "paid_reach")
    private Integer paidReach;

    @Column(name = "total_impressions")
    private Integer totalImpressions;

    @Column(name = "engaged_users")
    private Integer engagedUsers;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "share_count")
    private Integer shareCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "collected_at", nullable = false)
    @JsonProperty("collected_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectedAt;
}
