package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDataDeletionRequest;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbDataDeletionRequest;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbDataDeletionRequestRepository;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FacebookDataDeletionServiceTest {

    private final FbDataDeletionRequestRepository repository = mock(FbDataDeletionRequestRepository.class);
    private final FacebookProperties properties = properties();
    private final FacebookSignedRequestService signedRequestService = new FacebookSignedRequestService(
            properties,
            new ObjectMapper()
    );
    private final FacebookDataDeletionService service = new FacebookDataDeletionService(repository, signedRequestService);

    @Test
    void receiveStoresDeletionRequestAndReturnsConfirmationCode() throws Exception {
        when(repository.save(any(FbDataDeletionRequest.class))).thenAnswer(invocation -> {
            FbDataDeletionRequest request = invocation.getArgument(0);
            request.setId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
            return request;
        });

        var response = service.receive(new FacebookDataDeletionRequest(
                "reviewer@example.com",
                "fb-user-1",
                "page-1",
                null,
                "please delete"
        ));

        assertEquals(FbDataDeletionRequest.Status.RECEIVED, response.status());
        assertEquals("FB-DEL-11111111", response.confirmationCode());
        assertTrue(response.message().contains("ghi nhận"));
        verify(repository).save(any(FbDataDeletionRequest.class));
    }

    @Test
    void receiveParsesValidSignedRequestUserId() throws Exception {
        when(repository.save(any(FbDataDeletionRequest.class))).thenAnswer(invocation -> {
            FbDataDeletionRequest request = invocation.getArgument(0);
            request.setId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
            return request;
        });
        String signedRequest = signedRequest(Map.of(
                "algorithm", "HMAC-SHA256",
                "user_id", "fb-user-from-signed-request"
        ), properties.getAppSecret());

        service.receive(new FacebookDataDeletionRequest(null, null, null, signedRequest, null));

        ArgumentCaptor<FbDataDeletionRequest> captor = ArgumentCaptor.forClass(FbDataDeletionRequest.class);
        verify(repository).save(captor.capture());
        assertEquals("fb-user-from-signed-request", captor.getValue().getFbUserId());
        assertTrue(captor.getValue().getNote().contains("signed_request_verified=true"));
    }

    @Test
    void receiveRejectsInvalidSignedRequestSignature() {
        String signedRequest = signedRequest(Map.of("user_id", "fb-user"), "wrong-secret");

        assertThrows(CustomException.class, () ->
                service.receive(new FacebookDataDeletionRequest(null, null, null, signedRequest, null))
        );
        verify(repository, never()).save(any());
    }

    private static FacebookProperties properties() {
        FacebookProperties properties = new FacebookProperties();
        properties.setAppId("app-id");
        properties.setAppSecret("secret");
        properties.setGraphVersion("v25.0");
        properties.setGraphHost("https://graph.facebook.com");
        properties.setRedirectUri("https://example.com/callback");
        properties.setVerifyToken("verify");
        return properties;
    }

    private static String signedRequest(Map<String, Object> payload, String secret) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(payload));
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String encodedSignature = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(encodedPayload.getBytes(StandardCharsets.UTF_8)));
            return encodedSignature + "." + encodedPayload;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
