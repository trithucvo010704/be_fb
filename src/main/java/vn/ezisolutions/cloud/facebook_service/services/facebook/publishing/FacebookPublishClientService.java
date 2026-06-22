package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;
import vn.ezisolutions.cloud.facebook_service.gateway.facebook.FacebookPublishGateway;

import java.io.File;
import java.net.URI;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookPublishClientService {
    private static final Logger log = LoggerFactory.getLogger(FacebookPublishClientService.class);
    private final FacebookPublishGateway publishClient;

    public String postToGraph(String path, Map<String, Object> payload) throws FacebookApiException {
        try {
            String[] parts = parsePath(path);
            String pageId = parts[0];
            String edge = parts[1];

            Map<String, Object> response = publishClient.publishToEdge(pageId, edge, payload);
            if (response != null && response.containsKey("id")) {
                return response.get("id").toString();
            }
            throw new FacebookApiException(500, 0, "Phản hồi từ FB không có ID", null);
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi đăng bài: " + e.getMessage(), null);
        }
    }

    public boolean updatePost(String postId, Map<String, Object> payload) throws FacebookApiException {
        try {
            Map<String, Object> response = publishClient.updatePost(postId, payload);
            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi sửa bài: " + e.getMessage(), null);
        }
    }

    public void uploadVideoBinary(String uploadUrl, String filePath, String token) throws FacebookApiException {
        File file = new File(filePath);
        String authHeader = "OAuth " + token;
        String fileSize = String.valueOf(file.length());

        try {
            publishClient.uploadVideoBinary(
                    URI.create(uploadUrl),
                    authHeader,
                    "0",
                    fileSize,
                    MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    new FileSystemResource(file)
            );
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi upload video binary: " + e.getMessage(), null);
        }
    }

    public void uploadVideoByUrl(String uploadUrl, String fileUrl, String token) throws FacebookApiException {
        String authHeader = "OAuth " + token;

        try {
            publishClient.uploadVideoByUrl(
                    URI.create(uploadUrl),
                    authHeader,
                    fileUrl
            );
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi upload video by URL: " + e.getMessage(), null);
        }
    }

    public Map<String, Object> postFormData(String path, MultiValueMap<String, String> formData) throws FacebookApiException {
        try {
            String[] parts = parsePath(path);
            String pageId = parts[0];
            String edge = parts[1];

            return publishClient.publishFormData(pageId, edge, formData);
        } catch (FacebookApiException e) {
            throw e;
        } catch (Exception e) {
            throw new FacebookApiException(-1, 0, "Lỗi hệ thống khi gửi form data: " + e.getMessage(), null);
        }
    }

    private String[] parsePath(String path) throws FacebookApiException {
        if (path == null || path.isEmpty()) {
            throw new FacebookApiException(-1, 0, "Đường dẫn Graph API không được để trống", null);
        }
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        String[] parts = cleanPath.split("/");
        if (parts.length < 2) {
            throw new FacebookApiException(-1, 0, "Đường dẫn Graph API không hợp lệ (yêu cầu định dạng /{id}/{edge}): " + path, null);
        }
        return parts;
    }
}
