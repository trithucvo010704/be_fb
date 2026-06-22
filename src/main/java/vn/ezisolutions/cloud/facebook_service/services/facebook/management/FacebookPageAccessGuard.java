package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPage;
import vn.ezisolutions.cloud.facebook_service.repositories.facebook.FbPageRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FacebookPageAccessGuard {
    private final FbPageRepository pageRepository;

    public FbPage requireConnectedPage(String pageIdOrFbPageId) throws CustomException {
        FbPage page = findPage(pageIdOrFbPageId);
        if (page.getConnectionStatus() != FbPage.ConnectionStatus.CONNECTED) {
            throw new CustomException(400, "Fanpage đã bị disconnect");
        }
        if (page.getTokenStatus() != FbPage.TokenStatus.ACTIVE) {
            throw new CustomException(400, "Token Fanpage không còn hoạt động");
        }
        return page;
    }

    private FbPage findPage(String pageIdOrFbPageId) throws CustomException {
        if (pageIdOrFbPageId == null || pageIdOrFbPageId.isBlank()) {
            throw new CustomException(400, "Thiếu pageId");
        }
        try {
            return pageRepository.findById(UUID.fromString(pageIdOrFbPageId))
                    .orElseThrow(() -> new CustomException(404, "Không tìm thấy Fanpage"));
        } catch (IllegalArgumentException ignored) {
            return pageRepository.findByFbPageId(pageIdOrFbPageId)
                    .orElseThrow(() -> new CustomException(404, "Không tìm thấy Fanpage"));
        }
    }
}
