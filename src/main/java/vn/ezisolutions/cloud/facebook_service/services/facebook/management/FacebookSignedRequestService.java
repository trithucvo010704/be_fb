package vn.ezisolutions.cloud.facebook_service.services.facebook.management;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.properties.FacebookProperties;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FacebookSignedRequestService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final FacebookProperties properties;
    private final ObjectMapper objectMapper;

    public Map<String, Object> verifyAndParse(String signedRequest) throws CustomException {
        if (signedRequest == null || signedRequest.isBlank()) {
            return Map.of();
        }
        String[] parts = signedRequest.split("\\.", 2);
        if (parts.length != 2) {
            throw new CustomException(400, "signed_request không hợp lệ");
        }

        byte[] signature = decodeBase64Url(parts[0]);
        byte[] expected = hmacSha256(parts[1], properties.getAppSecret());
        if (!MessageDigest.isEqual(signature, expected)) {
            throw new CustomException(400, "signed_request sai chữ ký");
        }

        try {
            return objectMapper.readValue(decodeBase64Url(parts[1]), MAP_TYPE);
        } catch (Exception e) {
            throw new CustomException(400, "Không đọc được payload signed_request");
        }
    }

    private byte[] hmacSha256(String value, String secret) throws CustomException {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new CustomException(500, "Không verify được signed_request", e);
        }
    }

    private byte[] decodeBase64Url(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}

