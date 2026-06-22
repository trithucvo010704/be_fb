package vn.ezisolutions.cloud.facebook_service.core.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;

import java.io.IOException;
import java.net.URI;

public class FacebookResponseErrorHandler implements ResponseErrorHandler {
    private final DefaultResponseErrorHandler defaultHandler = new DefaultResponseErrorHandler();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return defaultHandler.hasError(response);
    }

    @Override
    public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
        handleFacebookError(response);
        defaultHandler.handleError(url, method, response);
    }

    private void handleFacebookError(ClientHttpResponse response) {
        try {
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                int code = error.path("code").asInt(-1);
                int subcode = error.path("error_subcode").asInt(0);
                String message = error.path("message").asText("Unknown Facebook API error");
                String traceId = error.path("fbtrace_id").asText(null);

                throw new FacebookApiException(code, subcode, message, traceId);
            }
        } catch (FacebookApiException fae) {
            throw fae;
        } catch (Exception ignored) {
        }
    }
}
