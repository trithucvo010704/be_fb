package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.dto.event.FbPostingPlanEvent;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPostRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostMediaRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPostRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacebookPostService {
    private static final Logger log = LoggerFactory.getLogger(FacebookPostService.class);

    private final FbPostRepository fbPostRepository;
    private final FbPostMediaRepository fbPostMediaRepository;

    public void savePostPlan(FbPostingPlanEvent event) {
        if (event.getPosts() == null || event.getPosts().isEmpty())
            return;

        for (FacebookPostRequest postRequest : event.getPosts()) {
            LocalDateTime scheduledTime = postRequest.getScheduledTime();
            if (scheduledTime == null) {
                scheduledTime = LocalDateTime.now();
            }

            FbPost newPost = FbPost.builder()
                    .pageId(postRequest.getPageId())
                    .content(postRequest.getContent())
                    .linkUrl(postRequest.getLink())
                    .postRole(postRequest.getPostRole() != null ? postRequest.getPostRole() : FbPost.PostRole.FEED)
                    .status(FbPost.PostStatus.PENDING)
                    .source(FbPost.Source.SYSTEM)
                    .scheduledTime(scheduledTime)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .createdBy(event.getOwnerId())
                    .build();

            FbPost savedPost = fbPostRepository.save(newPost);

            if (postRequest.getMedia() != null && !postRequest.getMedia().isEmpty()) {
                List<FbPostMedia> mediaList = new ArrayList<>();
                for (int i = 0; i < postRequest.getMedia().size(); i++) {
                    var item = postRequest.getMedia().get(i);
                    FbPostMedia media = FbPostMedia.builder()
                            .postId(savedPost.getId())
                            .mediaUrl(item.getUrl())
                            .mediaType(item.getType())
                            .mediaOrder(i)
                            .uploadStatus(FbPostMedia.UploadStatus.PENDING)
                            .build();
                    mediaList.add(media);
                }
                fbPostMediaRepository.saveAll(mediaList);
            }
        }
    }
}
