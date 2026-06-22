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
@Table(name = "fb_posts", indexes = {
    @Index(name = "idx_fb_posts_page", columnList = "page_id"),
    @Index(name = "idx_fb_posts_status", columnList = "status"),
    @Index(name = "idx_fb_posts_scheduled", columnList = "scheduled_time")
})
public class    FbPost {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "page_id", nullable = false, length = 50)
    private String pageId;

    @Column(name = "fb_post_id", unique = true, length = 50)
    private String fbPostId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "link_url", length = 512)
    private String linkUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_role", length = 20)
    @Builder.Default
    private PostRole postRole = PostRole.FEED;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private PostStatus status = PostStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 20)
    @Builder.Default
    private Source source = Source.SYSTEM;

    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name = "posted_time")
    private LocalDateTime postedTime;

    @Column(name = "created_by", length = 50)
    private String createdBy;

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

    public enum PostRole {
        FEED, STORY, REELS
    }

    public enum PostStatus {
        PENDING, PROCESSING, SUCCESS, FAILED, PUBLISHED
    }

    public enum Source {
        SYSTEM,
        FACEBOOK
    }
}
