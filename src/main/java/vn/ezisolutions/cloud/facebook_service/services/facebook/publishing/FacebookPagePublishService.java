package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPublishPostRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookPagePostResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePostMedia;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPagePostMediaRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPagePostRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookPagePublishService {

    private final FacebookPageAccessGuard pageAccessGuard;
    private final FacebookTokenService tokenService;
    private final FacebookPublishClientService publishClientService;
    private final FbPagePostRepository postRepository;
    private final FbPagePostMediaRepository mediaRepository;

    @Transactional
    public FacebookPagePostResponse publish(
            String pageId,
            FacebookPublishPostRequest request,
            AuthorizedUser owner
    ) throws CustomException {
        if (!StringUtils.hasText(request.content()) && !request.hasImages()) {
            throw new CustomException(400, "Bài đăng cần có content hoặc imageUrls");
        }

        FbPage page = pageAccessGuard.requireConnectedPage(pageId);
        String token = tokenService.getPageAccessToken(page.getFbPageId());
        if (!StringUtils.hasText(token)) {
            throw new CustomException(400, "Page token không khả dụng hoặc đã hết hạn");
        }

        FbPagePost post = postRepository.save(FbPagePost.builder()
                .pageId(page.getId())
                .content(request.content() == null ? "" : request.content())
                .status(FbPagePost.Status.PUBLISHING)
                .createdBy(owner == null ? null : owner.getId())
                .build());
        List<FbPagePostMedia> media = saveMedia(post, request.imageUrls());

        try {
            Map<String, Object> response = request.hasImages()
                    ? publishPhoto(page, token, request)
                    : publishFeed(page, token, request.content());
            post.setFbPostId(resolvePostId(response));
            post.setStatus(FbPagePost.Status.PUBLISHED);
            post.setPublishedAt(LocalDateTime.now());
        } catch (FacebookApiException e) {
            post.setStatus(FbPagePost.Status.FAILED);
            post.setErrorMessage(e.getUserFriendlyMessage());
            postRepository.save(post);
            throw new CustomException(e.getHttpStatus(), "Đăng bài Facebook thất bại: " + e.getUserFriendlyMessage());
        }

        return FacebookPagePostResponse.from(postRepository.save(post), media);
    }

    public Page<FacebookPagePostResponse> listPosts(String pageId, int page, int limit) throws CustomException {
        FbPage fbPage = pageAccessGuard.requireConnectedPage(pageId);
        return postRepository.findByPageIdOrderByCreatedAtDesc(fbPage.getId(), toPageRequest(page, limit))
                .map(post -> FacebookPagePostResponse.from(
                        post,
                        mediaRepository.findByPostIdOrderByMediaOrderAsc(post.getId())
                ));
    }

    private Map<String, Object> publishFeed(FbPage page, String token, String content) {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("message", content);
        form.add("access_token", token);
        return publishClientService.postFormData("/" + page.getFbPageId() + "/feed", form);
    }

    private Map<String, Object> publishPhoto(FbPage page, String token, FacebookPublishPostRequest request) {
        String firstImageUrl = request.imageUrls().stream()
                .filter(StringUtils::hasText)
                .findFirst()
                .orElseThrow();

        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("url", firstImageUrl);
        if (StringUtils.hasText(request.content())) {
            form.add("caption", request.content());
        }
        form.add("access_token", token);
        return publishClientService.postFormData("/" + page.getFbPageId() + "/photos", form);
    }

    private List<FbPagePostMedia> saveMedia(FbPagePost post, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<FbPagePostMedia> media = imageUrls.stream()
                .filter(StringUtils::hasText)
                .map(url -> FbPagePostMedia.builder()
                        .postId(post.getId())
                        .mediaType(FbPagePostMedia.MediaType.IMAGE)
                        .mediaUrl(url)
                        .mediaOrder(imageUrls.indexOf(url))
                        .build())
                .toList();
        return mediaRepository.saveAll(media);
    }

    private String resolvePostId(Map<String, Object> response) throws FacebookApiException {
        if (response == null) {
            throw new FacebookApiException(500, 0, "Facebook không trả dữ liệu bài đăng", null);
        }
        Object postId = response.get("post_id");
        if (postId == null) {
            postId = response.get("id");
        }
        if (postId == null) {
            throw new FacebookApiException(500, 0, "Facebook không trả post id", null);
        }
        return postId.toString();
    }

    private PageRequest toPageRequest(int page, int limit) {
        int safePage = Math.max(page, 1) - 1;
        int safeLimit = Math.min(Math.max(limit, 1), 100);
        return PageRequest.of(safePage, safeLimit);
    }
}
