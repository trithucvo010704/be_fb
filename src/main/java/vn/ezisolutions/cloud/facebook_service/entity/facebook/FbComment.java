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
@Table(name = "fb_comments", indexes = {
    @Index(name = "idx_fb_comments_post", columnList = "post_id"),
    @Index(name = "idx_fb_comments_parent", columnList = "parent_id")
})
public class FbComment {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(name = "fb_comment_id", unique = true, nullable = false, length = 50)
    private String fbCommentId;

    @Column(name = "parent_id", length = 50)
    private String parentId;

    @Column(name = "sender_id", length = 50)
    private String senderId;

    @Column(name = "sender_name", length = 255)
    private String senderName;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_suggest", length = 20)
    private ActionSuggest actionSuggest;

    @Column(name = "comment_suggest", columnDefinition = "TEXT")
    private String commentSuggest;

    @Column(name = "inbox_suggest", columnDefinition = "TEXT")
    private String inboxSuggest;

    @Builder.Default
    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Builder.Default
    @Column(name = "is_replied")
    private Boolean isReplied = false;

    @Builder.Default
    @Column(name = "is_messaged")
    private Boolean isMessaged = false;

    @Column(name = "fb_reply_id", length = 50)
    private String fbReplyId;

    @Column(name = "created_time_fb")
    private LocalDateTime createdTimeFb;

    @Column(name = "processed_at")
    @JsonProperty("processed_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;

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

    public enum ActionSuggest {
        HIDE,
        REPLY,
        INBOX,
        NONE
    }
}
