package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.dto.request.FacebookDataDeletionRequest;
import vn.ezisolutions.cloud.facebook_service.dto.response.FacebookDataDeletionResponse;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbDataDeletionRequest;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbDataDeletionRequestRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookDataDeletionService {

    private final FbDataDeletionRequestRepository repository;
    private final FacebookSignedRequestService signedRequestService;

    @Transactional
    public FacebookDataDeletionResponse receive(FacebookDataDeletionRequest request) throws CustomException {
        Map<String, Object> signedPayload = signedRequestService.verifyAndParse(request == null ? null : request.signedRequest());
        String fbUserId = firstText(request == null ? null : request.fbUserId(), signedPayload.get("user_id"));

        FbDataDeletionRequest saved = repository.save(FbDataDeletionRequest.builder()
                .requesterEmail(request == null ? null : request.requesterEmail())
                .fbUserId(fbUserId)
                .fbPageId(request == null ? null : request.fbPageId())
                .requestType(FbDataDeletionRequest.RequestType.DELETE_FACEBOOK_DATA)
                .status(FbDataDeletionRequest.Status.RECEIVED)
                .note(buildNote(request, signedPayload))
                .requestedAt(LocalDateTime.now())
                .build());

        return new FacebookDataDeletionResponse(
                saved.getId(),
                saved.getStatus(),
                confirmationCode(saved.getId()),
                "Yêu cầu xóa dữ liệu đã được ghi nhận"
        );
    }

    private String buildNote(FacebookDataDeletionRequest request, Map<String, Object> signedPayload) {
        if (request == null) {
            return signedPayload.isEmpty() ? null : "signed_request_verified=true";
        }
        StringBuilder note = new StringBuilder();
        if (request.note() != null && !request.note().isBlank()) {
            note.append(request.note());
        }
        if (request.signedRequest() != null && !request.signedRequest().isBlank()) {
            if (!note.isEmpty()) {
                note.append("\n");
            }
            note.append("signed_request_verified=true");
            if (signedPayload.get("algorithm") != null) {
                note.append("\nalgorithm=").append(signedPayload.get("algorithm"));
            }
        }
        return note.isEmpty() ? null : note.toString();
    }

    private String firstText(String explicitValue, Object fallbackValue) {
        if (explicitValue != null && !explicitValue.isBlank()) {
            return explicitValue;
        }
        return fallbackValue == null ? null : fallbackValue.toString();
    }

    private String confirmationCode(UUID id) {
        return "FB-DEL-" + id.toString().substring(0, 8).toUpperCase();
    }
}
