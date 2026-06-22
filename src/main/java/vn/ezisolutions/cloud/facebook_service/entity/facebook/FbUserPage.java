package vn.ezisolutions.cloud.facebook_service.entity.facebook;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fb_user_pages", indexes = {
    @Index(name = "idx_fb_user_pages_user", columnList = "fb_user_id"),
    @Index(name = "idx_fb_user_pages_page", columnList = "fb_page_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_fb_user_pages_user_page", columnNames = {"fb_user_id", "fb_page_id"})
})
public class FbUserPage {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @Column(name = "fb_user_id", nullable = false, length = 50)
    private String fbUserId;

    @Column(name = "fb_page_id", nullable = false, length = 50)
    private String fbPageId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
