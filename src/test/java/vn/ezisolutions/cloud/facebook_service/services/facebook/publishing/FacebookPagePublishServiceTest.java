package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import org.junit.jupiter.api.Test;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookPublishPostRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPagePostMedia;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPagePostMediaRepository;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPagePostRepository;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookPageAccessGuard;
import vn.ezisolutions.cloud.facebook_service.services.facebook.management.FacebookTokenService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FacebookPagePublishServiceTest {

    private final FacebookPageAccessGuard pageAccessGuard = mock(FacebookPageAccessGuard.class);
    private final FacebookTokenService tokenService = mock(FacebookTokenService.class);
    private final FacebookPublishClientService publishClientService = mock(FacebookPublishClientService.class);
    private final FbPagePostRepository postRepository = mock(FbPagePostRepository.class);
    private final FbPagePostMediaRepository mediaRepository = mock(FbPagePostMediaRepository.class);
    private final FacebookPagePublishService service = new FacebookPagePublishService(
            pageAccessGuard,
            tokenService,
            publishClientService,
            postRepository,
            mediaRepository
    );

    @Test
    void publishImagePostStoresPostAndMedia() throws Exception {
        FbPage page = page();
        when(pageAccessGuard.requireConnectedPage("page-1")).thenReturn(page);
        when(tokenService.getPageAccessToken("page-1")).thenReturn("page-token");
        when(postRepository.save(any(FbPagePost.class))).thenAnswer(invocation -> {
            FbPagePost post = invocation.getArgument(0);
            if (post.getId() == null) {
                post.setId(UUID.randomUUID());
            }
            return post;
        });
        when(mediaRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(publishClientService.postFormData(eq("/page-1/photos"), any()))
                .thenReturn(Map.of("post_id", "fb-post-1"));

        var response = service.publish(
                "page-1",
                new FacebookPublishPostRequest("Nội dung", List.of("https://example.com/image.jpg")),
                AuthorizedUser.builder().id("demo").build()
        );

        assertEquals("fb-post-1", response.fbPostId());
        assertEquals(FbPagePost.Status.PUBLISHED, response.status());
        assertEquals(1, response.media().size());
        verify(publishClientService).postFormData(eq("/page-1/photos"), any());
    }

    private FbPage page() {
        return FbPage.builder()
                .id(UUID.randomUUID())
                .fbPageId("page-1")
                .pageName("Page")
                .connectionStatus(FbPage.ConnectionStatus.CONNECTED)
                .tokenStatus(FbPage.TokenStatus.ACTIVE)
                .build();
    }
}

