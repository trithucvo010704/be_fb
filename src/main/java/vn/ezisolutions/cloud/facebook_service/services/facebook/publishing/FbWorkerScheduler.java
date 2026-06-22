package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "facebook.features.scheduled-posts", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class FbWorkerScheduler {
    private static final Logger log = LoggerFactory.getLogger(FbWorkerScheduler.class);

    private final FbPostRepository fbPostRepository;
    private final FbPublishWorkerService fbPublishWorkerService;

    @Scheduled(fixedDelay = 10000)
    public void scanAndProcessPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<FbPost> pendingPosts = fbPostRepository.findPendingPostsToPublish(
                FbPost.PostStatus.PENDING, now);
        if (pendingPosts.isEmpty())
            return;
        log.info(">>> Worker tìm thấy {} bài viết cần đăng.", pendingPosts.size());
        for (FbPost post : pendingPosts) {
            try {
                post.setStatus(FbPost.PostStatus.PROCESSING);
                fbPostRepository.save(post);
                fbPublishWorkerService.processPost(post);
            } catch (Exception e) {
                log.error("Lỗi ngoại lệ tại Scheduler cho Post ID: {}", post.getId(), e);
                post.setStatus(FbPost.PostStatus.FAILED);
                fbPostRepository.save(post);
            }
        }
    }
}
